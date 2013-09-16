package com.berico.similarity;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collection;

/**
 * A set of utilities for Manipulating Character Arrays
 * @author Richard Clayton (Berico Technologies)
 * @date December 4, 2010
 * @author Yen-Fu Luo
 * @date December 6, 2012
 */
public class WordVectorUtils {

	/**
	 * Get the word set of shorter length from two strings
	 * @param string1 First String
	 * @param string2 Second String
	 * @return the word set of shorter length
	 */
	public static Collection<String> getFeatureVector(Collection<String> wordVector1, Collection<String> wordVector2){
		Collection<String> wordVector;
		if (wordVector1.size() > wordVector2.size())
			wordVector = wordVector2;
		else
			wordVector = wordVector1;
		return wordVector;
	}
	
	/**
	 * Convert a string[] to a set of words
	 * @param string input string
	 * @return set of words
	 */
	public static Collection<String> sentenceToWordSet(String[] string){
		Collection<String> wordSet = new TreeSet<String>();
		for(String words : string){
			wordSet.add(words);
		}
		return wordSet;
	}
	
}
