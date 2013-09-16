package britannica;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


public class Reference implements Serializable{

	public static void main(String[] args) {
				
		// ToDo if n is too big, return an error
		// ToDo Deal with argument format and check
		
		if (args.length == 0)
			return;
		
		if (args[0].equalsIgnoreCase("--train"))
		{
			if (args.length != 5)
				return;
			else
			{
				ngramObject = new Reference();
				ngramObject.training_data_file = args[1];
				ngramObject.ngram_length = Integer.parseInt(args[2]);
				ngramObject.model_file = args[4];
				ngramObject.train(ngramObject.training_data_file, ngramObject.ngram_length, ngramObject.model_file);
				
				// Serialize object as model
				try
				{
					ObjectOutput out = new ObjectOutputStream(new FileOutputStream(ngramObject.model_file));
					out.writeObject(ngramObject);
					out.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		else if (args[0].equalsIgnoreCase("--test"))
		{
			if (args.length != 6)
				return;
			else
			{
				try
				{
					// Deserialize (Read in Model)
					ObjectInput oi = new ObjectInputStream(new FileInputStream(args[3]));
					ngramObject = (Reference) oi.readObject();
					oi.close();
					
					ngramObject.test_data_file = args[1];
					ngramObject.model_file = args[3];
					ngramObject.delta = Double.parseDouble(args[5]);
					
					System.out.println("The log probability of the text: " + ngramObject.test(ngramObject.test_data_file, ngramObject.model_file, ngramObject.delta));
				} catch (Exception ex) {ex.printStackTrace();}
			}
		}
		else return;
	}
	
	private static Reference ngramObject;
	private int ngram_length;
	private String training_data_file;
	private String test_data_file;
	private String model_file;
	private Double delta;
	private Hashtable<List<String>, Double> nGramTable = new Hashtable<List<String>, Double>();
	private Hashtable<List<String>, Double> nMinusOneGramTable = new Hashtable<List<String>, Double>();
	private Set<String> vocabularies =  new LinkedHashSet<String>();
	
	private void train(String training_data_file, int ngram_length, String model_file)
	{		
		try
		{
			BufferedReader br  = new BufferedReader(new FileReader(training_data_file));
			String sentence = "";
			String[] tempSentencesentenceTokens = null;
			ArrayList<String> sentenceTokens = null;
			
			int lastNMinusOneGramIndex = 0; // Last n-1 gram index for individual sentence
			
			
			// Get vocabularies from training data set ; Include OOV, <s>, and </s> in the vocabularies
			vocabularies.add("<s>");
			vocabularies.add("</s>");
			
			// While loop for processing each sentence
			while ((sentence = br.readLine()) != null)
			{
				if (sentence.length() == 0) continue; // Deal with the last empty sentence
				
				tempSentencesentenceTokens = tokenize(sentence); // Tokenize sentence
				sentenceTokens = new ArrayList<String>(Arrays.asList(tempSentencesentenceTokens)); // For simple array operation

				// Get vocabularies from training data set
				for (int i = 0; i < sentenceTokens.size(); i++) vocabularies.add(sentenceTokens.get(i));
				
				// Include <s> and </s> for the beginning and end of the sentence
				sentenceTokens.add(0, "<s>");
				sentenceTokens.add("</s>");
				
				lastNMinusOneGramIndex = sentenceTokens.size() - (ngram_length -1); // This handles the possible out of index exception in the following for loop
				ArrayList<String> nMinusOneGramTokens = null;
				ArrayList<String> nGramTokens = null;
				
				for (int i = 0; i <= lastNMinusOneGramIndex; i++)
				{
					// Count n-1 gram based on the sentence
					nMinusOneGramTokens = new ArrayList<String>(sentenceTokens.subList(i, i+(ngram_length-1)));
					if (nMinusOneGramTable.get(nMinusOneGramTokens) == null)
						nMinusOneGramTable.put(nMinusOneGramTokens, 1.0d);
					else
						nMinusOneGramTable.put(nMinusOneGramTokens, nMinusOneGramTable.get(nMinusOneGramTokens) + 1.0d);

					// Count n gram based on the sentence
					if (i == lastNMinusOneGramIndex)
						continue;
					else
					{
						nGramTokens = new ArrayList<String>(sentenceTokens.subList(i, i+ngram_length));
						
						if (nGramTable.get(nGramTokens) == null)
							nGramTable.put(nGramTokens, 1.0d);
						else
							nGramTable.put(nGramTokens, nGramTable.get(nGramTokens) + 1.0d);
					}
				}
			}
			
			br.close();
			
			// Add UNK to ngram hash table based on n-1 gram
			ArrayList<String> tempNgramKeyList = null;

			for (List<String> tempKeys : nMinusOneGramTable.keySet())
			{
				tempNgramKeyList = new ArrayList<String>(tempKeys);
				tempNgramKeyList.add("UNK");
				if (nGramTable.get(tempNgramKeyList) == null)
				{
					nGramTable.put(tempNgramKeyList, 0.0d);
				}
			}
		}
		catch (Exception ex) {ex.printStackTrace();}
	}
	
	private Double test(String test_data_file, String model_file, Double additive_constant)
	{ 	
		Double textProbability = 0.0d;
		try
		{
			BufferedReader br  = new BufferedReader(new FileReader(test_data_file));
			String sentence = "";
			String[] tempSentencesentenceTokens = null;
			ArrayList<String> sentenceTokens = null;
			
			// Update the count of nGramTable and nMinusOneGramTable based on delta
			int nGramTableSize = nGramTable.size();
			ArrayList<List<String>> tempNGramKeys = new ArrayList<List<String>>(nGramTable.keySet());
			for (int i = 0; i < nGramTableSize; i++)
				nGramTable.put(tempNGramKeys.get(i), (nGramTable.get(tempNGramKeys.get(i)) + delta));
			
			int nMinusOneGramTableSize = nMinusOneGramTable.size();
			ArrayList<List<String>> tempNMinusOneGramKeys = new ArrayList<List<String>>(nMinusOneGramTable.keySet());
			for (int i = 0; i < nMinusOneGramTableSize; i++)
				nMinusOneGramTable.put(tempNMinusOneGramKeys.get(i), (nMinusOneGramTable.get(tempNMinusOneGramKeys.get(i)) + (delta * (double) vocabularies.size())));
			
			// While loop for processing each sentence
			Double sentenceProbability;
			ArrayList<String> tempNGram;
			ArrayList<String> tempNMinusOneGram;
			Double tempXCount; // Special Case for non ngram
			Double tempXMinusOneCount; // Special Case for non ngram
			ArrayList<String> tempXGram; // Special Case for non ngram
			ArrayList<String> tempXMinusOneGram; // Special Case for non ngram
			
			while ((sentence = br.readLine()) != null)
			{
				if (sentence.length() == 0) continue; // Deal with the last empty sentence

				tempSentencesentenceTokens = tokenize(sentence); // Tokenize sentence
				sentenceTokens = new ArrayList<String>(Arrays.asList(tempSentencesentenceTokens)); // For simple array operation

				// Go through tokens to change unseen word to UNK
				int sentenceLength = sentenceTokens.size();
				for (int i = 0; i < sentenceLength; i++)
				{
					if (!ngramObject.vocabularies.contains(sentenceTokens.get(i)))
					{
						sentenceTokens.remove(i);
						sentenceTokens.add(i, "UNK");
					}
				}
				
				// Include <s> and </s> for the beginning and end of the sentence
				sentenceTokens.add(0, "<s>");
				sentenceTokens.add("</s>");

				// Additional processing for ngram
				// Quadruple gram example: <s> How are you ? </s>
				// p(How | <s>) * p(are | <s> How) * p(you | <s> How are) * p(? | How are you) * p(</s> | are you ?)
				// Get n gram from the second token (Not from <s>)
				// Need further calculation for p(How | <s>) and p(are | <s> How) ==> not quadruple gram
				tempNGram = null;
				tempNMinusOneGram = null;
				sentenceProbability = 0.0d;
				for (int i = 1; i < sentenceTokens.size(); i++)
				{
					
					if (i < ngram_length-1)
						tempNGram = new ArrayList<String>(sentenceTokens.subList(0, (i+1)));
					else
						tempNGram = new ArrayList<String>(sentenceTokens.subList(i-(ngram_length-1), (i+1)));

					tempNMinusOneGram = new ArrayList<String>(tempNGram.subList(0, tempNGram.size()-1));
					
					// There exists two cases for dealing with UNK (Use trigram for example)
					// First, it is an ngram with last UNK gram. (V1, V2, UNK) ==> Need to consider the count for (V1, V2)
					// Second, it is an ngram with UNK gram in the middle or beginning.
					// (V1, UNK, V2) ==> There is no count for (V1, UNK) ==> It does not exist in the model
					if (tempNGram.size() < ngram_length)
					{
						tempXCount = 0.0d;
						tempXMinusOneCount = 0.0d;
						tempXGram = null;
						tempXMinusOneGram = null;
						for (int j = 0; j < nMinusOneGramTable.size(); j++)
						{
							tempXGram = new ArrayList<String>(tempNMinusOneGramKeys.get(j).subList(0, tempNGram.size()));
							tempXMinusOneGram = new ArrayList<String>(tempNMinusOneGramKeys.get(j).subList(0, (tempNGram.size()-1)));
							if (tempXGram.equals(tempNGram))
							{
								tempXCount += nMinusOneGramTable.get(tempNMinusOneGramKeys.get(j));
							}
							if (tempXMinusOneGram.equals(tempNMinusOneGram))
							{
								tempXMinusOneCount += nMinusOneGramTable.get(tempNMinusOneGramKeys.get(j));
							}
						}
						
						if (tempXMinusOneCount != 0)
						{
							if (tempXCount != 0)
								sentenceProbability += calculatLogProbability(tempXCount, tempXMinusOneCount);
							else
								sentenceProbability += calculatLogProbability(delta, tempXMinusOneCount);
						}
						else
						{
							sentenceProbability +=  calculatLogProbability(delta, ((double) vocabularies.size() * delta));
						}
					}
					else
					{
						if (nMinusOneGramTable.get(tempNMinusOneGram) != null)
						{
							if (nGramTable.get(tempNGram) != null)
								sentenceProbability +=  calculatLogProbability(nGramTable.get(tempNGram), nMinusOneGramTable.get(tempNMinusOneGram));
							else
								sentenceProbability += calculatLogProbability(delta, nMinusOneGramTable.get(tempNMinusOneGram));
						}
						else
						{
							sentenceProbability +=  calculatLogProbability(delta, ((double) vocabularies.size() * delta));
						}
					}
						
				}
				textProbability += sentenceProbability;
			}
		} catch (Exception ex) {ex.printStackTrace();}
		
		return textProbability;
	}
	
	private static Double calculatLogProbability(Double numerator, Double denominator)
	{
		
		return Math.log10(numerator / denominator);
	}
	
	private static String[] tokenize(String sentence)
	{
		InputStream modelIn = null;
		String sentenceTokens[] = null;

		try {
			modelIn = ngramObject.getClass().getResourceAsStream("/en-token.bin");
			TokenizerModel model = new TokenizerModel(modelIn);
			Tokenizer tokenizer = new TokenizerME(model);
			sentenceTokens = tokenizer.tokenize(sentence);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
		return sentenceTokens;
	}

}
