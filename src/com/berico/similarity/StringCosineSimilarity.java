package com.berico.similarity;

import static com.berico.similarity.WordVectorUtils.*;
import static com.berico.similarity.VectorMath.*;

import java.util.Collection;
import java.util.Vector;

public class StringCosineSimilarity {

	
	/**
	 * Calculate the similarity of two strings using Cosine Similarity
	 * @param stringOne
	 * @param stringTwo
	 * @return cosine of the two angles (percentage of similarity)
	 */
	public double calculate(String[] stringOne, String[] stringTwo) {
		Collection<String> wordVector1 = sentenceToWordSet(stringOne);
		Collection<String> wordVector2 = sentenceToWordSet(stringTwo);
		Collection<String> featureVector = getFeatureVector(wordVector1, wordVector2);
		
		Collection<Integer> stringOneOccurrenceVector = createOccurrenceVector(wordVector1, featureVector);
		Collection<Integer> stringTwoOccurrenceVector = createOccurrenceVector(wordVector2, featureVector);

		int dotProduct = 0;
		//This should be an unnecessary exception since we're submitting the union
		//of both strings
		try {
			dotProduct = dotp(stringOneOccurrenceVector, stringTwoOccurrenceVector);
		} catch (VectorMathException e){
			e.printStackTrace();
			System.out.println(stringOneOccurrenceVector);
			System.out.println(stringTwoOccurrenceVector);
			return -2;
		}
		
		double vectorOneMagnitude = magnitude(stringOneOccurrenceVector);
		double vectorTwoMagnitude = magnitude(stringTwoOccurrenceVector);
			
		return dotProduct / (vectorOneMagnitude * vectorTwoMagnitude);
	}
	
	/**
	 * Get the Occurrence Vector
	 * @param wordVector
	 * @param featureVector
	 * @return occurrenceVector
	 */
	private static Collection<Integer> createOccurrenceVector(Collection<String> wordVector, Collection<String> featureVector){
		Collection<Integer> occurrenceVector = new Vector<Integer>();
		for(String word : featureVector){
			occurrenceVector.add((Integer) wordOccurrence(wordVector, word));
		}
		return occurrenceVector;
	}

	/**
	 * Whether a word occurs in a string
	 * @param string Input String
	 * @param word Input String
	 * @return Boolean of Occurrence (0, 1) (false, true)
	 */
	private static int wordOccurrence(Collection<String> wordVector, String word){
		int occurrence = 0;
		if (wordVector.contains(word))
			occurrence = 1;
		return occurrence;
	}
}
