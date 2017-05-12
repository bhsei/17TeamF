package classification;

import java.util.*;
import java.util.Map.Entry;

public class Bow {
	public static Map<String, Integer> text2Bow(String text)
	{
		String[] w=text.split(" ");
//		System.out.println(w.length);
		Map<String, Integer> bow=new HashMap<>();
		for(String word:w)
		{
			if(word.length()<1) continue;
			if(bow.containsKey(word))
				bow.put(word, bow.get(word)+1);
			else
				bow.put(word, 1);
		}
		return bow;
	}
	/**
	 * 将一篇文章切分好的wordList转成词袋表示
	 * @param wordList
	 * @return
	 */
	public static HashMap<String, Integer> text2Bow(List<String> wordList)
	{
		
		HashMap<String, Integer> bow=new HashMap<>();
		for(String word:wordList)
		{
			if(word.length()<1) continue;
			if(bow.containsKey(word))
				bow.put(word, bow.get(word)+1);
			else
				bow.put(word, 1);
		}
		return bow;
	}
	public static void mergetf(Map<String, Integer> bow,Map<String, Integer> tfAll)
	{
		Set<Entry<String, Integer>> entrySet=bow.entrySet();
		for(Entry<String, Integer> entry:entrySet)
		{
			String word=entry.getKey();
			int count=entry.getValue();
			if(tfAll.containsKey(word))
				tfAll.put(word, tfAll.get(word)+count);
			else
				tfAll.put(word, count);
		}
	}
	public static void mergdf(Map<String, Integer> bow,Map<String, Integer> dfAll)
	{
		Set<Entry<String, Integer>> entrySet=bow.entrySet();
		for(Entry<String, Integer> entry:entrySet)
		{
			String word=entry.getKey();
			int count=entry.getValue();
			if(dfAll.containsKey(word))
				dfAll.put(word, dfAll.get(word)+1);
			else
				dfAll.put(word, 1);
		}
	}
//	public static Map<String, Double> calTfIdf(Map<String, Integer> tfAll,Map<String, Integer> dfAll,int docTotal,boolean normalizing)
//	{
//		Map<String, Double> tfIdf=new HashMap<>();
//		Set<Entry<String, Integer>> entrySet=tfAll.entrySet();
//		double tfidfMax=0;
//		double tfidfMin=Double.MAX_VALUE;
//		for(Entry<String, Integer> entry:entrySet)
//		{
//			String word=entry.getKey();
//			int tf=entry.getValue();
//			if(dfAll.containsKey(word))
//			{
//				int df=dfAll.get(word);
//				double tfidf=tf*Math.log((double)docTotal/df);
//				if(tfidf>tfidfMax)
//					tfidfMax=tfidf;
//				if(tfidf<tfidfMin)
//					tfidfMin=tfidf;
//				tfIdf.put(word, tfidf);
//			}
//		}
//		if(normalizing)
//		{
//			for(String key:tfIdf.keySet())
//			{
//				double tfidf=tfIdf.get(key);
//				tfIdf.put(key, (tfidf-tfidfMin)/(tfidfMax-tfidfMin));
//			}
//		}
//		return tfIdf;
//	}
	/**
	 * 计算tfidf并返回字符串形式的结果Map     
	 * e.g.“中国		345		34		45.777”
	 * @param tfAll
	 * @param dfAll
	 * @param docTotal
	 * @param normalizing
	 * @return
	 */
	public static Map<String, String> calTfIdfReturnStr(Map<String, Integer> tfAll,Map<String, Integer> dfAll,int docTotal,boolean normalizing)
	{
		Map<String, Double> tfIdf=new HashMap<>();
		Map<String, String> tfIdfStr=new HashMap<>();
		Set<Entry<String, Integer>> entrySet=tfAll.entrySet();
		double tfidfMax=0;
		double tfidfMin=Double.MAX_VALUE;
		for(Entry<String, Integer> entry:entrySet)
		{
			String word=entry.getKey();
			int tf=entry.getValue();
			if(dfAll.containsKey(word))
			{
				int df=dfAll.get(word);
				double tfidf=tf*Math.log((double)docTotal/df); 
				if(tfidf>tfidfMax)
					tfidfMax=tfidf;
				if(tfidf<tfidfMin)
					tfidfMin=tfidf;
				tfIdf.put(word, tfidf);
			}
		}
		if(normalizing)
		{
			Map<String, Double> tfIdfNor=new HashMap<>();
			for(String key:tfIdf.keySet())
			{
				double tfidf=tfIdf.get(key);
				tfIdfNor.put(key, (tfidf)/(tfidfMax));
			}
			tfIdf=tfIdfNor;
		}
		for(String key:tfAll.keySet())
		{
			if(dfAll.containsKey(key)&&tfIdf.containsKey(key))
			{
				tfIdfStr.put(key, String.format("%d\t%d\t%.3f", tfAll.get(key),dfAll.get(key),tfIdf.get(key)));
			}
		}
		return tfIdfStr;
	}
	public static Map<String, String> calTfIdfeReturnStr(Map<String, Integer> tfAll,Map<String, Integer> dfAll,int docTotal,Map<String, Double> entropyAll,boolean normalizing)
	{
		Map<String, Double> tfIdf=new HashMap<>();
		Map<String, String> tfIdfStr=new HashMap<>();
		Set<Entry<String, Integer>> entrySet=tfAll.entrySet();
		double tfidfMax=0;
		double tfidfMin=Double.MAX_VALUE;
		for(Entry<String, Integer> entry:entrySet)
		{
			String word=entry.getKey();
			int tf=entry.getValue();
			if(dfAll.containsKey(word)&&entropyAll.containsKey(word))
			{
				int df=dfAll.get(word);
				double e=entropyAll.get(word);
				double tfidf=tf*Math.log((double)docTotal/df)*e; 
				if(tfidf>tfidfMax)
					tfidfMax=tfidf;
				if(tfidf<tfidfMin)
					tfidfMin=tfidf;
				tfIdf.put(word, tfidf);
			}
		}
		if(normalizing)
		{
			Map<String, Double> tfIdfNor=new HashMap<>();
			for(String key:tfIdf.keySet())
			{
				double tfidf=tfIdf.get(key);
				tfIdfNor.put(key, (tfidf)/(tfidfMax));
			}
			tfIdf=tfIdfNor;
		}
		for(String key:tfAll.keySet())
		{
			if(dfAll.containsKey(key)&&tfIdf.containsKey(key))
			{
				tfIdfStr.put(key, String.format("%d\t%d\t%.3f", tfAll.get(key),dfAll.get(key),tfIdf.get(key)));
			}
		}
		return tfIdfStr;
	}
	public static List<Entry<String,String>> sortMap(Map<String, String> map)
	{
		List<Entry<String,String>> mappingList = null;
		mappingList = new ArrayList<Entry<String,String>>(map.entrySet());
		Collections.sort(mappingList, new Comparator<Entry<String,String>>(){
			public int compare(Entry<String,String> mapping1,Entry<String,String> mapping2){
				double tfidf1=Double.valueOf(mapping1.getValue().split("\t")[2]);
				double tfidf2=Double.valueOf(mapping2.getValue().split("\t")[2]);
				if(tfidf1-tfidf2<0)
					return 1;
				else if(tfidf1==tfidf2)
					return 0;
				return -1;
				} 
			}); 
		 return mappingList;
	}
	public static List<Entry<String,String>> filter(List<Entry<String,String>> mapList,int keyWordNum,int minDf)
	{
		List<Entry<String,String>> linkedList=new LinkedList<>(mapList);
		List<Entry<String,String>> result=new ArrayList<>();
		for(int i=0;i<linkedList.size()&&i<keyWordNum;)
		{
			if(Integer.valueOf(linkedList.get(i).getValue().split("\t")[1])<minDf)
				linkedList.remove(i);
			else
				i++;
		}
		for(int i=0;i<linkedList.size()&&i<keyWordNum;i++)
		{
			result.add(linkedList.get(i));
		}
		return result;
	}
	public static Double log2(double x)
	{
		return Math.log(x)/Math.log(2);
	}
	public static Map<String, Double> calEtropy(Map<String, Map<Integer,Integer>> wordClassesDocDis,Map<Integer,Integer> classesDocNum)
	{
		int docCnt=0;
		for(Integer cla:classesDocNum.keySet())
			docCnt+=classesDocNum.get(cla);
		Map<String, Double> entropyAll=new HashMap<>();
		for(String word:wordClassesDocDis.keySet())
		{
			Double entropy=0.0;
			Map<Integer, Integer> oneWordclassesDocDis=wordClassesDocDis.get(word);
			for(Integer cla:oneWordclassesDocDis.keySet())
			{
				int oneWordOneClaDocNum=oneWordclassesDocDis.get(cla);
				int oneClaDocNumAll=classesDocNum.get(cla);
				double p=((double)oneWordOneClaDocNum)/oneClaDocNumAll;
				entropy-=log2(p)*p*oneClaDocNumAll/docCnt;
			}
			entropy=1/entropy;
			entropyAll.put(word, entropy);
		}
 		return entropyAll;
	}
	public static void main(String[] args) {
		
	}
}
