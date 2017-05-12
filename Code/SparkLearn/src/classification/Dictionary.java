package classification;

import utils.TextUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Dictionary {
	public int docTotal;
	public int wordTotal;
	public Map<String, Integer> w2i=new HashMap<>();//word-index
	public Map<Integer, String> i2w=new HashMap<>();//index-word
	public Map<Integer, Integer> i2tf=new HashMap<>();//index-tf
	public Map<Integer, Double> i2tfidf=new HashMap<>();//index-tfidf
	public Map<Integer, Integer> i2df=new HashMap<>();//index-df
	public Dictionary()
	{
		
	}
	/**
	 * 
	 * @param wordList
	 * @param docTotal
	 */
	public Dictionary(List<Entry<String, String>> wordList,int docTotal)//
	{
		wordTotal=wordList.size();
		this.docTotal=docTotal;
		for(int i=0;i<wordList.size();i++)
		{
			Entry<String, String> entry=wordList.get(i);
			String word=entry.getKey();
			String value=entry.getValue();
			int tf=Integer.valueOf(value.split("\t")[0]);
			int df=Integer.valueOf(value.split("\t")[1]);
			double tfidf=Double.valueOf(value.split("\t")[2]);
			int index=i+1;
			w2i.put(word, index);
			i2w.put(index, word);
			i2tf.put(index, tf);
			i2tfidf.put(index, tfidf);
			i2df.put(index, df);
		}
	}
	public boolean contains(String word)
	{
		return w2i.containsKey(word);
	}
	public void save(String savePath) throws IOException
	{
		TextUtil.saveStrToFile("docTotal="+docTotal, savePath, false);
		for(int i=1;i<=wordTotal;i++)
		{
			String line=String.format("%d\t%s\t%d\t%d\t%.3f", i,i2w.get(i),i2tf.get(i),i2df.get(i),i2tfidf.get(i));
			TextUtil.saveStrToFile(line, savePath, true);
		}
	}
	public void load(String path) throws IOException 
	{
		BufferedReader bf=new BufferedReader(new FileReader(new File(path)));
		String firstLine=bf.readLine();
		docTotal=Integer.valueOf(firstLine.split("=")[1]);
		wordTotal=0;
		while(true)
		{
			String line=bf.readLine();
			if(line==null) break;
			wordTotal++;
			int index=Integer.valueOf(line.split("\t")[0]);
			String word=line.split("\t")[1];
			int tf=Integer.valueOf(line.split("\t")[2]);
			int df=Integer.valueOf(line.split("\t")[3]);
			double tfidf=Double.valueOf(line.split("\t")[4]);
			w2i.put(word, index);
			i2w.put(index, word);
			i2tf.put(index, tf);
			i2tfidf.put(index, tfidf);
			i2df.put(index, df);
		}
		bf.close();
	}
	
}
