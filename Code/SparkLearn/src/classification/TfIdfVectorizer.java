package classification;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TfIdfVectorizer {
	public int textCase=0;//-1:raw 0:lowerCase 1:upperCase
	public boolean rmLinks=true;
	public String[] rmSet={"<br/>","&nbsp","&nbsp;"};
	public int keyWordsNum=1000;
	public int minDf=10;
	public String dictSavePath="dict.txt";
	public boolean normalizing=true;
	public List<String> stopWords=new ArrayList<>();
	public Dictionary dictionary=new Dictionary();
	public TfIdfVectorizer() {
		// TODO Auto-generated constructor stub
	}
	private static TfIdfVectorizer instance = null;
	private static boolean loaded = false;

	public synchronized static TfIdfVectorizer Instance() {
		if (instance == null) {
			instance = new TfIdfVectorizer();
		}
		return instance;
	}
	public synchronized static boolean isLoaded() {
		return loaded;
	}
	/**
	 * 构建词典时候用
	 * @param textCase
	 * @param rmLinks
	 * @param rmSet
	 * @param keyWordsNum
	 * @param minDf
	 * @param dictSavePath
	 * @param stopWords
	 */
	public TfIdfVectorizer(int textCase,boolean rmLinks,String[] rmSet,int keyWordsNum,int minDf,String dictSavePath,List<String> stopWords,boolean normalizing) {
		// TODO Auto-generated constructor stub
		this.textCase=textCase;
		this.rmLinks=rmLinks;
		this.rmSet=rmSet;
		this.keyWordsNum=keyWordsNum;
		this.minDf=minDf;
		this.dictSavePath=dictSavePath;
		this.stopWords=stopWords;
		this.normalizing=normalizing;
	}
	public TfIdfVectorizer(boolean normalizing) {
		// TODO Auto-generated constructor stub
		this.normalizing=normalizing;
	}

	
	/**
	 * 只训练模型（字典），不将输入文本转为向量
	 * @param articleList
	 * @throws IOException
	 */
	public void fit(List<String> articleList) throws IOException
	{
		//清洗每一篇文章（去除超链接，特殊无意义字符等）
		List<String> articleListClear=new ArrayList<>();
		for(String text:articleList)
			articleListClear.add(wash(text));
//		makeDict(articleListClear);
		Map<String, Integer> tfAll=new HashMap<>();//每个词的总词频
		Map<String, Integer> dfAll=new HashMap<>();//每个词的总文档频率
		Map<String, Double> tfidfAll=new HashMap<>();//每个词的if-idf
		Map<String, String> tfidfStrAll=new HashMap<>();//每个词的 tf df tfidf 格式化字符串 e.g.“中国		345		34		45.777”
		//对文章进行分词，去停用词，转成词袋模型
		int docTotal=0;
		List<HashMap<String, Integer>> bowAll=new ArrayList<>();
		for(String article:articleListClear)
		{
			//分词
//			List<String> wordList=wordSeg(article);
			List<String> wordList=Arrays.asList(article.split(" "));
			//去停用词
			List<String> wordListAfterRm=new ArrayList<>();
			for(String word:wordList)
				if(!stopWords.contains(word))
					wordListAfterRm.add(word);
			//转词袋
			HashMap<String, Integer> bow=Bow.text2Bow(wordListAfterRm);
			bowAll.add(bow);
			//合并计算总的 tf 和 df
			Bow.mergetf(bow, tfAll);
			Bow.mergdf(bow, dfAll);
			docTotal++;
		}
		//格式化tf-idf结果
		tfidfStrAll=Bow.calTfIdfReturnStr(tfAll,dfAll,docTotal,true);
		//按照tfidf降序排序
		List<Entry<String, String>> sortedList=Bow.sortMap(tfidfStrAll);
		//通过保留的关键词总量和最小
		List<Entry<String, String>> filterdList=Bow.filter(sortedList, keyWordsNum, minDf);
		//构建词典
		dictionary=new Dictionary(filterdList, docTotal);
//		makeDict(articleListClear);		
	}
	
	public void fitWithEntropy(List<String> articleList,List<Integer> labelList)
	{
		List<String> articleListClear=new ArrayList<>();
		for(String text:articleList)
			articleListClear.add(wash(text));
//		makeDict(articleListClear);
		Map<String, Integer> tfAll=new HashMap<>();//每个词的总词频
		Map<String, Integer> dfAll=new HashMap<>();//每个词的总文档频率
		Map<String, Double> tfidfAll=new HashMap<>();//每个词的if-idf
		Map<String, String> tfidfStrAll=new HashMap<>();//每个词的 tf df tfidf 格式化字符串 e.g.“中国		345		34		45.777”
		Map<String, Double> tfidfeAll=new HashMap<>();
		Map<String, Double> entropyAll=new HashMap<>();
		Map<Integer,Integer> classesDocNum=new HashMap<>();
		Map<String, Map<Integer,Integer>> wordClassesDocDis=new HashMap<>();
		//对文章进行分词，去停用词，转成词袋模型
		int docTotal=0;
		List<HashMap<String, Integer>> bowAll=new ArrayList<>();
		for(int i=0;i<articleListClear.size();i++)
		{
			String article =articleListClear.get(i);
			int label=labelList.get(i);
			if(classesDocNum.containsKey(label))
				classesDocNum.put(label, classesDocNum.get(label)+1);
			else
				classesDocNum.put(label, 1);
			//分词
//			List<String> wordList=wordSeg(article);
			List<String> wordList=Arrays.asList(article.split(" "));
			//去停用词
			List<String> wordListAfterRm=new ArrayList<>();
			for(String word:wordList)
				if(!stopWords.contains(word))
					wordListAfterRm.add(word);
			//转词袋
			HashMap<String, Integer> bow=Bow.text2Bow(wordListAfterRm);
			for(String word:bow.keySet())
			{
				if(wordClassesDocDis.containsKey(word))
				{
					if(wordClassesDocDis.get(word).containsKey(label))
						wordClassesDocDis.get(word).put(label, wordClassesDocDis.get(word).get(label)+1);
					else
						wordClassesDocDis.get(word).put(label,1);
				}
				else
				{
					Map<Integer, Integer> map=new HashMap<>();
					map.put(label, 1);
					wordClassesDocDis.put(word, map);
				}
			}
			bowAll.add(bow);
			//合并计算总的 tf 和 df
			Bow.mergetf(bow, tfAll);
			Bow.mergdf(bow, dfAll);
			docTotal++;
		}
		entropyAll=Bow.calEtropy(wordClassesDocDis, classesDocNum);
		//格式化tf-idf结果
		tfidfStrAll=Bow.calTfIdfeReturnStr(tfAll,dfAll,docTotal,entropyAll,true);
		//按照tfidf降序排序
		List<Entry<String, String>> sortedList=Bow.sortMap(tfidfStrAll);
		//通过保留的关键词总量和最小
		List<Entry<String, String>> filterdList=Bow.filter(sortedList, keyWordsNum, minDf);
		//构建词典
		dictionary=new Dictionary(filterdList, docTotal);
	}
	
	/**
	 * 训练模型（词典），并将输入文本转化成向量
	 * @param articleList
	 * @return
	 * @throws IOException
	 */
	public List<List<Entry<Integer, Double>>>  fitAndTrans(List<String> articleList) throws IOException
	{
		fit(articleList);
		return trans(articleList);	
	}
	public List<List<Entry<Integer, Double>>>  fitWithEAndTrans(List<String> articleList,List<Integer> labelList) throws IOException
	{
		fitWithEntropy(articleList, labelList);
		return trans(articleList);	
	}
	public List<Entry<Integer, Double>> trans(String article) throws IOException
	{
		List<String> articleList=new ArrayList<>();
		articleList.add(article);
		return trans(articleList).get(0);
	}
	/**
	 * 只转文本为向量
	 * @param articleList
	 * @return
	 * @throws IOException
	 */
	public List<List<Entry<Integer, Double>>>  trans(List<String> articleList) throws IOException
	{
		//构建 词袋模型
		List<String> articleListClear=new ArrayList<>();
		
		for(String text:articleList)
			articleListClear.add(wash(text));
		List<HashMap<String, Integer>> bowAll=new ArrayList<>();
		for(String article:articleListClear)
		{
			List<Term> termlist= HanLP.segment(article);
			List<String> wordList= new ArrayList<>();
			for(Term term:termlist)
				wordList.add(term.word);
//			List<String> wordList=Arrays.asList(article.split(" "));
			List<String> wordListAfterRm=new ArrayList<>();
			if(stopWords!=null)
				for(String word:wordList)
					if(!stopWords.contains(word))
						wordListAfterRm.add(word);
			HashMap<String, Integer> bow=Bow.text2Bow(wordListAfterRm);
			bowAll.add(bow);
		}
		//构建tfidf向量
		List<List<Entry<Integer, Double>>> vectorList=new ArrayList<>();
		for(HashMap<String, Integer> articleBow:bowAll)
		{
			List<Entry<Integer, Double>> articleVector=new ArrayList<>();
			for(String word:articleBow.keySet())
			{
				if(dictionary.contains(word))
				{
					int index=dictionary.w2i.get(word);
					Double tfidf=(double)articleBow.get(word);
					Entry<Integer, Double> oneDimension=new Entry<Integer, Double>() {
						private final Integer key=new Integer(index);
					    private Double value;
						@Override
						public Double setValue(Double value) {
							// TODO Auto-generated method stub
							this.value=value;
							return this.value;
						}
						
						@Override
						public Double getValue() {
							// TODO Auto-generated method stub
							return this.value;
						}
						
						@Override
						public Integer getKey() {
							// TODO Auto-generated method stub
							return this.key;
						}
					};
					oneDimension.setValue(tfidf);
					articleVector.add(oneDimension);
				}
			}
			
			Collections.sort(articleVector, new Comparator<Entry<Integer,Double>>(){
				@Override
				public int compare(Entry<Integer, Double> arg0, Entry<Integer, Double> arg1) {
					// TODO Auto-generated method stub
					return arg0.getKey()>arg1.getKey()?1:-1;
				} 
				  }); 
			if(normalizing)
				normalize(articleVector);
			vectorList.add(articleVector);
//			System.out.println("lineSize"+article.size());
		}
		
		return vectorList;
	}
	public static void normalize(List<Entry<Integer, Double>> vectorList)
	{
		double max=-1;
		for(int i=0;i<vectorList.size();i++)
		{
			Entry<Integer, Double> entry=vectorList.get(i);
			double d=entry.getValue();
			if(d>max)
				max=d;
		}
		for(int i=0;i<vectorList.size();i++)
		{
			Entry<Integer, Double> entry=vectorList.get(i);
			vectorList.get(i).setValue(entry.getValue()/max);
		}
	}
	
	public String wash(String text)
	{
		if(textCase==0)
			text=text.toLowerCase();
		else if(textCase==1)
			text=text.toUpperCase();
		
		if(rmLinks)
		{
			Pattern pattern=Pattern.compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*");
			Matcher matcher = pattern.matcher(text);
			text=matcher.replaceAll(" ");
			pattern=Pattern.compile("(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w{2,3}){1,3})");
			matcher = pattern.matcher(text);
			text=matcher.replaceAll(" ");
		}
		
		for(String s:rmSet)
			text=text.replaceAll(s, " ");
		
		text=text.replaceAll("[^\u4e00-\u9fa5a-zA-Z]", " ");
		text=text.replaceAll("\\s+", " ");
		return text;
	}
	
	public static void main(String[] args) {
//		String text="j1   就 1j";
//		System.out.println(new TfIdfVectorizerImp().wash(text));
	}
	
	
}
