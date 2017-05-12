package classification;

import libsvm.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by 111111 on 2017/5/11.
 */
public class TextClassifier {
    private static svm_parameter param;		// set by parse_command_line
    private static svm_problem prob;		// set by read_problem
    private static svm_model model;
    private static String input_file_name;		// set by parse_command_line
    private static String model_file_name;		// set by parse_command_line
    private static String error_msg;
    private static int cross_validation;
    private static int nr_fold;
    private static TextClassifier instance = null;
    private static boolean loaded = false;
    public TextClassifier() {
        // TODO Auto-generated constructor stub
        setPara();
    }
    public synchronized static TextClassifier Instance() {
        if (instance == null) {
            instance = new TextClassifier();
        }
        return instance;
    }
    public synchronized static boolean isLoaded() {
        return loaded;
    }
    private static svm_print_interface svm_print_null = new svm_print_interface()
    {
        public void print(String s) {}
    };
    private static svm_print_interface svm_print_stdout = new svm_print_interface()
    {
        public void print(String s)
        {
            System.out.print(s);
        }
    };
    private static svm_print_interface svm_print_string = svm_print_stdout;

    static void info(String s)
    {
        svm_print_string.print(s);
    }
    public void setPara()
    {
        param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.degree = 3;
        param.gamma = 0;
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 1;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        cross_validation = 5;
    }
    public void loadModel(String modelPath) throws IOException
    {
        model= svm.svm_load_model(modelPath);
        loaded=true;
    }
    public void makeProblem(List<List<Entry<Integer, Double>>> X,List<Integer> Y)
    {
        Vector<Double> vy = new Vector<>();
        Vector<svm_node[]> vx = new Vector<>();

        int max_index = 0;
        for(int i=0;i<X.size();i++)//每一条数据
        {
            List<Entry<Integer, Double>> line=X.get(i);
            int m=line.size();
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)//每一(非零)维度
            {
                x[j]=new svm_node();
                x[j].index=line.get(j).getKey();
                x[j].value=line.get(j).getValue();
            }
            if(m>0) max_index = Math.max(max_index, x[m-1].index);
            vx.addElement(x);
        }
        for(int i=0;i<Y.size();i++)//每一条数据
        {
            int label=Y.get(i);
            vy.add((double)label);
        }
        prob=new svm_problem();
        prob.l=vy.size();
        prob.x=new svm_node[prob.l][];
        for(int i=0;i<prob.l;i++)
            prob.x[i] = vx.elementAt(i);
        prob.y =new double[prob.l];
        for(int i=0;i<prob.l;i++)
            prob.y[i] = vy.elementAt(i);

        if(param.gamma == 0 && max_index > 0)
            param.gamma = 1.0/max_index;

        if(param.kernel_type == svm_parameter.PRECOMPUTED)
            for(int i=0;i<prob.l;i++)
            {
                if (prob.x[i][0].index != 0)
                {
                    System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
                    System.exit(1);
                }
                if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
                {
                    System.err.print("Wrong input format: sample_serial_number out of range\n");
                    System.exit(1);
                }
            }


    }
    public void makeProblem(String inputPath) throws IOException
    {
        BufferedReader fp = new BufferedReader(new FileReader(inputPath));
        Vector<Double> vy = new Vector<>();
        Vector<svm_node[]> vx = new Vector<>();
        int max_index = 0;

        while(true)
        {
            String line = fp.readLine();
            if(line == null) break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            vy.addElement(atof(st.nextToken()));
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = atoi(st.nextToken());
                x[j].value = atof(st.nextToken());
            }
            if(m>0) max_index = Math.max(max_index, x[m-1].index);
            vx.addElement(x);
        }

        prob = new svm_problem();
        prob.l = vy.size();
        prob.x = new svm_node[prob.l][];
        for(int i=0;i<prob.l;i++)
            prob.x[i] = vx.elementAt(i);
        prob.y = new double[prob.l];
        for(int i=0;i<prob.l;i++)
            prob.y[i] = vy.elementAt(i);

        if(param.gamma == 0 && max_index > 0)
            param.gamma = 1.0/max_index;

        if(param.kernel_type == svm_parameter.PRECOMPUTED)
            for(int i=0;i<prob.l;i++)
            {
                if (prob.x[i][0].index != 0)
                {
                    System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
                    System.exit(1);
                }
                if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
                {
                    System.err.print("Wrong input format: sample_serial_number out of range\n");
                    System.exit(1);
                }
            }

        fp.close();
    }
    public void train() throws IOException
    {

        model = svm.svm_train(prob,param);
//		model_file_name="model";
//		svm.svm_save_model(model_file_name,model);
////		predict();
        return ;
    }
    public void saveModel(String model_file_name) throws IOException
    {
        svm.svm_save_model(model_file_name,model);
    }

    public int predictOne(List<Entry<Integer, Double>> X,double minPrb)
    {
        List<List<Entry<Integer, Double>>> xList=new ArrayList<List<Entry<Integer, Double>>>();
        xList.add(X);
        return predict(xList,minPrb).get(0);
    }
    public List<Integer> predict(List<List<Entry<Integer, Double>>> X,double minPrb)
    {
//		int svm_type=svm.svm_get_svm_type(model);
//		int nr_class=svm.svm_get_nr_class(model);
        List<Integer> result=new ArrayList<Integer>();
        for(int i=0;i<X.size();i++)//每一条数据
        {
            List<Entry<Integer, Double>> line=X.get(i);
            int m=line.size();
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)//每一(非零)维度
            {
                x[j]=new svm_node();
                x[j].index=line.get(j).getKey();
                x[j].value=line.get(j).getValue();
            }
            if(model == null)
                System.err.println("model is null.");
            /******无概率模型******/
//			int v =(int) svm.svm_predict(model,x);
//			result.add(v);
            double [] prob_estimates=new double[20];
            double resultprob=svm.svm_predict_probability(model, x,prob_estimates);
            System.out.println("resultprob"+resultprob);
            double maxPrb=0.0;
            for(int k=0;k<14;k++)
            {
                System.out.println("prob_estimates["+k+"]="+prob_estimates[k]);
                maxPrb=maxPrb>prob_estimates[k]?maxPrb:prob_estimates[k];
            }
            if(maxPrb<minPrb)//抛弃
                result.add(0);
            else
                result.add((int)resultprob);
        }
        return result;
    }

    public void predictTest(String inputPath) throws IOException
    {
        int correct = 0;
        int total = 0;
        BufferedReader bf=new BufferedReader(new FileReader(new File(inputPath)));
        int i=0;
        while(true)//每一条数据
        {
            i++;
            if(i%100==0)
                System.out.println(i);
            String lineTxt=bf.readLine();
            if(lineTxt==null) break;
            List<Entry<Integer, Double>> line=new ArrayList<Entry<Integer, Double>>();
            String[] w=lineTxt.split("\t");

            int m=w.length-1;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)//每一(非零)维度
            {
                x[j]=new svm_node();
                x[j].index=atoi(w[j+1].split(":")[0]);
                x[j].value=atof(w[j+1].split(":")[1]);
            }
            int v =(int) svm.svm_predict(model,x);
            int target=atoi(w[0]);
            if(v == target)
                ++correct;
            ++total;
        }
        TextClassifier.info("Accuracy = "+(double)correct/total*100+
                "% ("+correct+"/"+total+") (classification)\n");


        return;

    }
    public void predictTest(List<List<Entry<Integer, Double>>> X,List<Integer> Y)
    {
        int correct = 0;
        int total = 0;

        for(int i=0;i<X.size();i++)//每一条数据
        {
            List<Entry<Integer, Double>> line=X.get(i);
            int m=line.size();
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)//每一(非零)维度
            {
                x[j]=new svm_node();
                x[j].index=line.get(j).getKey();
                x[j].value=line.get(j).getValue();
            }
            int v =(int) svm.svm_predict(model,x);
            int target=Y.get(i);
            if(v == target)
                ++correct;
            ++total;
        }
        TextClassifier.info("Accuracy = "+(double)correct/total*100+
                "% ("+correct+"/"+total+") (classification)\n");


        return;

    }
    private static double atof(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }
    private static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    public static void main(String[] args) throws IOException {
        TextClassifier t = new TextClassifier();
        t.train();

    }
}
