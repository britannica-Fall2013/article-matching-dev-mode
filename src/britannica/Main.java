package britannica;

import info.bliki.wiki.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.berico.similarity.StringCosineSimilarity;


public class Main {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		// ********** (initialize only once) **********
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
//		engine.eval(new java.io.FileReader("C:/Users/Edward/Desktop/txtwiki_1.js"));
		try {
			engine.eval(new java.io.FileReader("/home/tsingh/WS/britannicaws/ArticleMatching/src/britannica/txtwiki.js"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Invocable inv = (Invocable) engine;
		
		// Query Wiki API
		StringBuilder fullText = new StringBuilder();
		try
		{	
			// ****** ToDo Same name but different person in different domain???
			
			// **************************************************************************************************************************
			
			int totalHits = 0;
			int srlimit = 50;
			int numberOfQuery = 0;
			
			HashMap<String, Double> map = null;
			ValueComparator vc = null;
			TreeMap<String, Double> sortMap = null;
			
			// Output File
			//File recordFile = new File("/home/tsingh/WS/britannicaws/ArticleMatching/NLP_Result/recordFile.txt");
			File recordFile = new File("/home/tsingh/WS/britannicaws/ArticleMatching/NLP_Dev_Result/recordFile.txt");
			if (!recordFile.exists()) recordFile.createNewFile();
			FileWriter recordFw = new FileWriter(recordFile.getAbsoluteFile());
			BufferedWriter recordBw = new BufferedWriter(recordFw);
			
			File output_File = null;
			FileWriter fw = null;
			BufferedWriter bw = null;
			
		    // File directory = new File("/home/tsingh/WS/britannicaws/ArticleMatching/Evaluation_Set/Britannica_Articles");
		    File directory = new File("/home/tsingh/WS/britannicaws/ArticleMatching/Development_Set/Britannica_Articles");
//		    System.out.println(Test.class.getProtectionDomain().getCodeSource().getLocation());
		    File[] files = directory.listFiles();
//		    String[] fileNames = directory.list();
//		    for (String file : fileNames)
		    for (File file : files)
		    {
		    	String[] tokens_1 = OpennlpUtils.tokenize(testData(file));
		    	
		    	//output_File = new File("/home/tsingh/WS/britannicaws/ArticleMatching/NLP_Result/" + file.getName().substring(0, file.getName().length() - 4) + ".csv");
		    	output_File = new File("/home/tsingh/WS/britannicaws/ArticleMatching/NLP_Dev_Result/" + file.getName().substring(0, file.getName().length() - 4) + ".csv");
		    	if (!output_File.exists()) output_File.createNewFile();
		    	fw = new FileWriter(output_File.getAbsoluteFile());
				bw = new BufferedWriter(fw);

		    	totalHits = 0;
		    	numberOfQuery = 0;
		    	
		    	map = new HashMap<String, Double>();
		    	vc = new ValueComparator(map);
		    	sortMap = new TreeMap<String,Double>(vc);

		    	// Even though, it is possible to have both search hits and suggestion search term from wikipedia.
		    	// Assumption: The suggestion from wikipedia is adopted.
		    	/*
		    	 * The following code is responsible for tokenizing the file into individual tokens
		    	 * which are being stored in a String array.
		    	 */
		    	// ******************************** ToDo ********************************
		    	String[] searchTokens = OpennlpUtils.tokenize(searchTerm(file));
		    	String searchTerm = "";
		    	if (searchTokens[1].equalsIgnoreCase(",")) 
		    	{
		    		if (searchTokens[2].equalsIgnoreCase("or"))
		    			/*
		    			 * searchTokens[0] expected to be first name of person
		    			 * searchTokens[3] expected to be an alternative to person's last name?
		    			 */
		    			searchTerm = searchTokens[0] + " " + searchTokens[3];
		    		else
		    			searchTerm = searchTokens[0] + " " + searchTokens[2];
		    	}
		    	else
		    	{
//		    		searchTerm = searchTokens[0] + " " + searchTokens[1];
		    		searchTerm = searchTokens[0];
		    	}
		    	System.out.print(searchTerm + " ; ");
		    	
		    	searchTerm = WikiUtils.fillSpace(searchTerm);
		    	
		    	String jsonString = WikiUtils.wikiQuery(searchTerm, "json", "1", 0, "simple");

		    	// Parse JSON object
		    	String suggestion = "";
		    	JSONObject json = new JSONObject(jsonString);
		    	JSONObject searchinfo = json.getJSONObject("query").getJSONObject("searchinfo");

		    	// Check to see if there is a suggestion search term
		    	if (searchinfo.has("suggestion"))
		    	{
		    		suggestion = searchinfo.getString("suggestion");
		    		// Use suggestion as a new search term for next wikipedia query
		    		// ToDo if there is no result, send another suggestion query based on current suggestion
		    		searchTerm = WikiUtils.fillSpace(suggestion);
		    		jsonString = WikiUtils.wikiQuery(searchTerm, "json", "1", 0, "simple");
		    		json = new JSONObject(jsonString);
		    		searchinfo = json.getJSONObject("query").getJSONObject("searchinfo");
		    	}

		    	System.out.println(file.getName() + " ; " + searchinfo.getInt("totalhits"));
		    	
		    	if (searchinfo.getInt("totalhits") > 2000)
		    	{
		    		System.out.println("*** Skipped *** " + file.getName());
		    		continue;
		    	}
		    	
		    	if (searchinfo.getInt("totalhits") == 0)
		    	{
		    		// ToDo Send another suggestion query to double check
		    		bw.write("No Related documents!!!");
		    		bw.close();
//		    		System.out.println("No Related documents!!!");
//		    		System.out.println(file.getName());
		    		continue;
		    	}
		    	else
		    	{
			    	totalHits = searchinfo.getInt("totalhits");
			    	numberOfQuery = (int) Math.ceil((double) totalHits / (double) srlimit);
			    	
			    	for (int z = 0; z < numberOfQuery; z++)
			    	{
			    		jsonString = WikiUtils.wikiQuery(searchTerm, "json", String.valueOf(srlimit), (z * srlimit), "generator");
			    		json = new JSONObject(jsonString);
			    		
			    		JSONObject pages = json.getJSONObject("query").getJSONObject("pages");
			    		Iterator iterator = pages.keys();
			    		JSONArray revisions = null;
			    		int beginOfArticle = 0;
			    		int endOfArticle = 0;
			    		String article = "";
			    		while(iterator.hasNext())
			    		{
			    			beginOfArticle = 0;
			    			endOfArticle = 0;
			    			String pageid = iterator.next().toString();
			    			revisions = pages.getJSONObject(pageid).getJSONArray("revisions");

			    			article = revisions.getJSONObject(0).getString("*");
/* ToDo document doesn't contain ''' or ''' appears later than end of article */
//			    			if (article.indexOf("'''") != -1) beginOfArticle = article.indexOf("'''"); // ToDo
//			    			else beginOfArticle = 0; 
			    			
			    			// Assumption: end of article are "See also==\n" or "References==\n" or "References ==\n"
			    			// ToDo check exception
			    			// ToDo check which index occurres first and set as end of article index
			    			if (article.indexOf("References==\n") != -1) endOfArticle = article.indexOf("References==\n");
			    			else if (article.indexOf("References ==\n") != -1) endOfArticle = article.indexOf("References ==\n");
			    			else if (article.indexOf("See also==\n") != -1) endOfArticle = article.indexOf("See also==\n");
			    			else endOfArticle = article.length();

			    			// Calculate cosine similarity
			    			// Britannica
//			    			String[] tokens_1 = OpennlpUtils.tokenize(testData(file));
			    			String[] tokens_2 = OpennlpUtils.tokenize(Jsoup.parse(WikiModel.toHtml(article.substring(beginOfArticle, endOfArticle))).text());

			    			StringCosineSimilarity similarity = new StringCosineSimilarity();
			    			double percentageSimilar = similarity.calculate(tokens_1, tokens_2) * 100;
			    			
			    			map.put(pages.getJSONObject(pageid).getString("fullurl"), percentageSimilar);
			    		}
			    		
			    		
			    	}
			    	
			    	sortMap.putAll(map);
			    	// ToDo Bug This implementation can not apply to method containsKey or sortMap.get
			    	for (Map.Entry<String, Double> entry : sortMap.entrySet())
			    	{
						bw.write("\"" + entry.getKey() + "\", " + entry.getValue() + "\n");
			    	}
			    	bw.close();
		    	}
		    		
		    	recordBw.write(file.getName() + "\n");
		    }
		    
		    recordBw.close();
			
		}
		catch (Exception ex){ex.printStackTrace();}
	}
	
	public static boolean isUpperCase(Character c)
	{
		if (Character.isLowerCase(c) || !Character.isLetter(c))
		{
			return false;
		}
		return true;
	}
	
	public static String temp()
	{
		BufferedReader br = null;
		String result = "";
		try {
			br = new BufferedReader (new FileReader("C:/Users/Edward/Desktop/wiki_json.txt"));
			result = br.readLine();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String testData(File file)
	{
		BufferedReader br = null;
		String result = "";
		String line = "";
		try {
			br = new BufferedReader (new FileReader(file));
			
			while ((line = br.readLine()) != null) {
				result = result.concat(line + " ");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String searchTerm(File file)
	{
		BufferedReader br = null;
		String result = "";
		String line = "";
		int count = 0;
		try {
			br = new BufferedReader (new FileReader(file));
			
			while ((line = br.readLine()) != null) {
				if (count == 2) break;
				result = result.concat(line + " ");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}

class ValueComparator implements Comparator<String> {
    
	Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }
	
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
}
