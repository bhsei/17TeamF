package sparkUse;

import classification.TextClassifier;
import classification.TfIdfVectorizer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;
import utils.TextUtil;

import java.util.*;

/**
 * Created by 111111 on 2017/3/28.
 */
public class SparkUse {
    public SparkUse() {
    }
    public static void kafkaSenderTest(String [] args)
    {
        System.out.println("#kafkaSender");
//        final String servers=args[1];
        final String acks=args[1];
        final String batchSize=args[2];
        final String lingerMs=args[3];
        final String bufferMemory=args[4];
        final String messageSize=args[5];
        final String topic=args[6];
        Properties props = new Properties();
        props.put("bootstrap.servers", "cat1:9092,cat2:9092,cat3:9092");
        props.put("acks", acks);//"all"
        props.put("retries", 0);
        props.put("batch.size", Integer.valueOf(batchSize));//16384
        props.put("linger.ms", Integer.valueOf(lingerMs));//1
        props.put("buffer.memory", Integer.valueOf(bufferMemory));//33554432
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(props);
        System.out.println("start produce");
        for(int i = 0; i < Integer.valueOf(messageSize); i++) {
            System.out.println(i);
            producer.send(new ProducerRecord<String, String>(topic, Integer.toString(i), Integer.toString(i)));
        }
        System.out.println("complete!");
        producer.close();
    }
    public static void readFromKafkaClassifySave2Kafka(String [] args) throws Exception
    {
        final String tplist=args[1];
        final String saveTopic=args[2];
        final String modelPath=args[3];
        final String dictPath=args[4];
        Properties props = new Properties();
        props.put("bootstrap.servers", "cat1:9092,cat2:9092,cat3:9092");
        props.put("acks", "all");//"all"
        props.put("retries", 0);
        props.put("batch.size", 16384);//16384
        props.put("linger.ms", 1);//1
        props.put("buffer.memory", 33554432);//33554432
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(props);
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "cat1:9092,cat2:9092,cat3:9092");
//        props.put("group.id", "111");
//        props.put("enable.auto.commit", "true");
//        props.put("auto.commit.interval.ms", "1000");
//        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        KafkaConsumer<String,String> consumer=new KafkaConsumer<>(props);
//        consumer.subscribe(Arrays.asList("foo", "bar"));
//        while (true) {
//            ConsumerRecords<String, String> records = consumer.poll(100);
//            for (ConsumerRecord<String, String> record : records)
//                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
//        }
        Map<String, Integer> topicMap = new HashMap<>();
        String[] topics = tplist.split(",");
        for (String topic: topics) {
            topicMap.put(topic, 1);
        }

        SparkConf sparkConf = new SparkConf().setAppName("StreamClassifier");
        final int durationInterval = 6000;
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(durationInterval));
        JavaPairReceiverInputDStream<String, String> messages
                = KafkaUtils.createStream(jssc,
                "10.2.2.141:2181,10.2.2.142:2181,10.2.2.143:2181",
                "grouptest", topicMap);
        JavaDStream<String> messagesDStream=messages.map(new Function<Tuple2<String, String>, String>() {
            @Override
            public String call(Tuple2<String, String> tuple2) throws Exception {
                return tuple2._2();
            }
        });
        JavaDStream<String> filteredMessageDStream=messagesDStream.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String s) throws Exception {
                String [] w=s.split("\1");
                if(w.length!=3)
                    return false;
                try {
                    if (Integer.valueOf(w[0]) % 2 == 1)
                        return false;
                }catch (Exception e)
                {
                    return false;
                }
                return true;
            }
        });
        JavaDStream<String> labeledMessageDStream=filteredMessageDStream.mapPartitions(new FlatMapFunction<Iterator<String>, String>() {
            @Override
            public Iterator<String> call(Iterator<String> stringIterator) throws Exception {
                List<String> labeledMessageList=new ArrayList<>();
                TextClassifier classifier;
                classifier= TextClassifier.Instance();
                TfIdfVectorizer tfIdfVectorizer;
                tfIdfVectorizer=TfIdfVectorizer.Instance();
                if(!TfIdfVectorizer.isLoaded())
                    tfIdfVectorizer.dictionary.load(dictPath);
                if (!TextClassifier.isLoaded())
                    classifier.loadModel(modelPath);
                while(stringIterator.hasNext())
                {
                    String singleMessage=stringIterator.next();
                    String text=singleMessage.split("\1")[2];
                    List<List<Map.Entry<Integer, Double>>> X= new ArrayList<>();
                    X.add(tfIdfVectorizer.trans(text));
                    String labeledSingleMessage=singleMessage+classifier.predict(X,0.2).get(0);
                    labeledMessageList.add(labeledSingleMessage);
                }
                return labeledMessageList.iterator();
            }
        });
        labeledMessageDStream.foreachRDD(new VoidFunction<JavaRDD<String>>() {
            @Override
            public void call(JavaRDD<String> stringJavaRDD) throws Exception {
                List<String> list=stringJavaRDD.collect();
                for(String msg:list){
                    System.out.println(msg);
                    producer.send(new ProducerRecord<String, String>(saveTopic,msg));
                }
                return;
            }
        });
        labeledMessageDStream.dstream().saveAsTextFiles("/user/pyj/jx/ClaRes","txt");
        jssc.start();
        jssc.awaitTermination();
    }
    public static void kafkaSender(String [] args)throws Exception
    {
        System.out.println("#kafkaSender");
        final String srcPath=args[1];
        final String topic=args[2];
        Properties props = new Properties();
        props.put("bootstrap.servers", "cat1:9092,cat2:9092,cat3:9092");
        props.put("acks", "all");//"all"
        props.put("retries", 0);
        props.put("batch.size", 16384);//16384
        props.put("linger.ms", 10);//1
        props.put("buffer.memory", 33554432);//33554432
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(props);
        System.out.println("start produce");
        List<String> weiboList=TextUtil.getAllSamplesList(srcPath);
        int count=Integer.valueOf(args[3]);
        Random random = new Random();
        while(count-->0) {
            for (int i = 0; i < weiboList.size(); i++)
                producer.send(new ProducerRecord<String, String>(topic,random.nextInt(1000)+"", weiboList.get(i)));
            Thread.sleep(1000*1);
        }
        System.out.println("complete!");
        producer.close();
    }
    public static void readkafkaPrint(String [] args)
    {
//        final String zklist = args[7];
//        final String groupname = args[8];
//        final String tplist = args[9];
//
//        final String saveConnect=args[16];
//        final String saveTopic=args[17];

//        Properties props=new Properties();
//        props.put("serializer.class","kafka.serializer.StringEncoder");
//        props.put("metadata.broker.list",saveConnect);
        Properties props = new Properties();
        props.put("bootstrap.servers", "cat1:9092,cat2:9092,cat3:9092");
        props.put("group.id", "test123456");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("weibo","weibo1"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records)
//                System.out.printf("offset = %d  splits = %d  value = %s", record.offset(),record.value().split("\1").length , record.value());
                System.out.printf("offset = %d   value = %s\n", record.offset(), record.value());
        }


    }
    public static void saveHdfsTextFile2Local(String [] args)throws Exception
    {
        String srcFilePath=args[1];
        String desFilePath=args[2];
        SparkConf sparkConf = new SparkConf().setAppName("saveHdfsTextFile2Local");//定义spark任务名称
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        JavaRDD<String> srcRDD= sc.textFile(srcFilePath);
        List<String> res=srcRDD.collect();
        TextUtil.saveAllSamples(res,desFilePath);
    }
    public static void readAndfilter(String [] args)
    {
        String srcFilePath=args[1];
        SparkConf sparkConf = new SparkConf().setAppName("HorizonSparkDemo");//定义spark任务名称
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        JavaRDD<String> srcRDD= sc.textFile(srcFilePath);
//        srcRDD.foreachPartition(new VoidFunction<Iterator<String>>() {
//            @Override
//            public void call(Iterator<String> stringIterator) throws Exception {
//                Random random = new Random();
//                while(stringIterator.hasNext())
//                    if(random.nextInt(1000)<100)
//                        System.out.println("***" + stringIterator.next() + "***");
//                return;
//            }
//        });
        JavaRDD<String> textSegRDD=srcRDD.filter(new Function<String, Boolean>(){
            @Override
            public Boolean call(String s) throws Exception {
                String[] w=s.split("\1");
                String textSeg=w[2];
                if(textSeg.split("\\s+").length<5)
                   return false;
                return true;
            }
        }).map(new Function<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return s.split("\1")[2];
            }
        });
        JavaRDD<String> words = textSegRDD.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\s+")).iterator();
            }
        });
        JavaPairRDD<String, Integer> word_1 = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String, Integer>(s, 1);
            }
        });
        JavaPairRDD<String, Integer> counts = word_1.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer+integer2;
            }
        });
        counts.saveAsTextFile(srcFilePath.replace("tagging","result"));

//        JavaPairRDD<String, Integer> counts = word_1.reduceByKey(new Function2<Integer, Integer, Integer>() {
//            @Override
//            public Integer call(Integer i1, Integer i2) {
//                return i1 + i2;
//            }
//        });
//        .flatMap(new FlatMapFunction<String, String>() {
//            @Override
//            public Iterator<String> call(String s) throws Exception {
//                List<String> res=new ArrayList<String>();
//                for(int i=0;i<s.length();i++)
//                    res.add(s.substring(i)+"|");
//                return res.iterator();
//            }
//        });

//        List<String> res=newRDD.collect();
//        List<String> src=srcRDD.collect();
//        for(int i=0;i<100;i++)
//            System.out.println(res.get(i));
//        System.out.println("src size="+src.size());
//        System.out.println("res size="+res.size());
    }
}
