package britannica;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
//import opennlp.tools.tokenize.*;
import opennlp.uima.tokenize.Tokenizer;


public class OpennlpUtils {

	private static OpennlpUtils utilsInstance;
	
	private static TreeSet<String> stopWords;
	static
	{
		InputStream modelIn = OpennlpUtils.getInstance().getClass().getResourceAsStream("./stopwords.txt"); // ToDo Stack overflow???
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(modelIn));
			stopWords = new TreeSet<String>();
			String line = null;
			while ((line = br.readLine()) != null)
			{
				stopWords.add(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
	}
	private OpennlpUtils()
	{
////		InputStream modelIn = Utils.getInstance().getClass().getResourceAsStream("./stopwords.txt"); // ToDo Stack overflow???
//		InputStream modelIn = null;
//		try {
//			modelIn = new FileInputStream("stopwords.txt"); // ToDo
//			BufferedReader br = new BufferedReader(new InputStreamReader(modelIn));
//			stopWords = new TreeSet<String>();
//			String line = null;
//			while ((line = br.readLine()) != null)
//			{
//				stopWords.add(line);
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (modelIn != null) {
//				try {
//					modelIn.close();
//				}
//				catch (IOException e) {
//				}
//			}
//		}
	}
	
	public static synchronized OpennlpUtils getInstance() {
	    if (utilsInstance == null) {
	    	utilsInstance = new OpennlpUtils();
	    }
	    return utilsInstance;
	}
	
	public static String[] sentenceDetector(String rawText)
	{
		String[] sentences =  null;
		InputStream modelIn = null;
		try
		{
			modelIn = new FileInputStream("en-sent.bin"); // ToDo
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
			sentences = sentenceDetector.sentDetect(rawText);
		}
		catch (IOException e) {e.printStackTrace();}
		finally
		{
			if (modelIn != null)
			{
				try
				{modelIn.close();}
				catch (IOException e) {e.printStackTrace();}
			}
		}
		
		return sentences;
	}
	
//	public static String[] sentenceDetector(File file)
//	{
//		String[] sentences =  null;
//		InputStream modelIn = null;
//		try
//		{
//			modelIn = new FileInputStream("en-sent.bin"); // ToDo
//			SentenceModel model = new SentenceModel(modelIn);
//			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
//			
//			BufferedReader br  = new BufferedReader(new FileReader("sample-training-data.txt"));
//			StringBuilder sb  = new StringBuilder();
//			String line = "";
//			while ((line = br.readLine()) != null) sb.append(" " + line + " ");
//			
//			sentences = sentenceDetector.sentDetect(rawText);
//		}
//		catch (IOException e) {e.printStackTrace();}
//		finally
//		{
//			if (modelIn != null)
//			{
//				try
//				{modelIn.close();}
//				catch (IOException e) {e.printStackTrace();}
//			}
//		}
//		
//		return sentences;
//	}
	
	private static InputStream modelIn;
	private static TokenizerModel model;
	private static TokenizerME tokenizer;
	
	static 
	{		
		try
		{
			modelIn = OpennlpUtils.getInstance().getClass().getResourceAsStream("./en-token.bin");
			model = new TokenizerModel(modelIn);
			//tokenizer = new TokenizerME(model);		
			tokenizer = new TokenizerME(model);
		}
		catch (Exception ex) {ex.printStackTrace();}
	}
	public static String[] tokenize(String sentence)
	{
//		InputStream modelIn = null;
		String tokens[] = null;

		try {
//			modelIn = OpennlpUtils.getInstance().getClass().getResourceAsStream("../models/en-token.bin");
//			modelIn = OpennlpUtils.getInstance().getClass().getResourceAsStream("./en-token.bin");
//			TokenizerModel model = new TokenizerModel(modelIn);
//			Tokenizer tokenizer = new TokenizerME(model);
			tokens = tokenizer.tokenize(sentence);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		finally {
//			if (modelIn != null) {
//				try {
//					modelIn.close();
//				}
//				catch (IOException e) {
//				}
//			}
//		}
		return tokens;
	}
	
	public static boolean isStopWord(String word)
	{
		boolean result = false;
//		if (Utils.getInstance().stopWords.contains(word))
		if (stopWords.contains(word))
			result = true;
		else
			result = false;
		return result;
	}
}
