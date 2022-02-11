package Model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

public class WordNetSimilarity {

	public static IRAMDictionary wordNetDictionary = null;
	public static WordnetStemmer wordNetStemmer = null;
	
	String wordNetFile = "lib" + File.separator + "WordNet-3.0" + File.separator + "dict";
	URL url;
	
	public WordNetSimilarity() throws Exception {
		url = new URL("file", null, wordNetFile);
		wordNetDictionary = new RAMDictionary(url, ILoadPolicy.NO_LOAD);
		wordNetDictionary.open();
		wordNetDictionary.load(true);
		wordNetStemmer = new WordnetStemmer(wordNetDictionary);
	}
	
	//Semantic Similarity Coefficient
	public static double findSemanticSimilarity(String word1, String word2) {

		//If words empty
		if (word1.equals("") || word2.equals("")) {
			return 0.0; 
		}
		
		word1 = word1.replaceAll("[^a-zA-Z0-9]", "");
		word2 = word2.replaceAll("[^a-zA-Z0-9]", "");

		List<String> stemsWord1 = wordNetStemmer.findStems(word1, POS.NOUN);
		List<String> stemsWord2 = wordNetStemmer.findStems(word2, POS.NOUN);
		
		//Word not found on wordnet Stemmer
		if (stemsWord1.isEmpty() || stemsWord2.isEmpty()) {
			return 0.0;
		}
		
		ArrayList<Set<ISynset>> wordVisited1List = new ArrayList<>();
		ArrayList<Set<ISynset>> wordVisited2List = new ArrayList<>();

		List<IWordID> word1IDList = new ArrayList<>();
		
		for (String stemmer : stemsWord1) {
			IIndexWord indexWord = wordNetDictionary.getIndexWord(stemmer, POS.NOUN);
			if (indexWord != null) {
				word1IDList.addAll(wordNetDictionary.getIndexWord(stemmer, POS.NOUN).getWordIDs());
			}
		}
		
		//Word not found in wordnet dictionnary
		if (word1IDList.isEmpty()) { 
			return 0.0; 
		}
		
		List<ISynset> synsetsWord1List = new ArrayList<>();
		
		for (IWordID wordID : word1IDList) { 
			synsetsWord1List.add(wordNetDictionary.getWord(wordID).getSynset());	
		
		}
		wordVisited1List.add(new HashSet<ISynset> (synsetsWord1List));
		
		List<IWordID> word2IDList = new ArrayList<>();
		
		for (String stemmer : stemsWord2) {
			IIndexWord indexWord = wordNetDictionary.getIndexWord(stemmer, POS.NOUN);
			if (indexWord != null) {
				word2IDList.addAll(wordNetDictionary.getIndexWord(stemmer, POS.NOUN).getWordIDs());
			}
		}
		
		if (word2IDList.isEmpty()) { 
			return 0.0; 
			
		}
		
		List<ISynset> synsetsWord2List = new ArrayList<>();
		
		for (IWordID wordID : word2IDList) { 
			synsetsWord2List.add(wordNetDictionary.getWord(wordID).getSynset()); 
		}
		
		wordVisited2List.add(new HashSet<ISynset> (synsetsWord2List));
		
		ISynset similarSynset = null;
		boolean similarWords = false;
		boolean firstLoop = false;
		boolean SecondLoop = false;
		
		int similarSynsetPos1 = -1;
		int similarSynsetPos2 = -1;
		
		while ((firstLoop == false && SecondLoop == false) && !similarWords) {
			
			int wordVisited1Size = wordVisited1List.size();
			int wordVisited2Size = wordVisited2List.size();
			
			if (!similarWords && !firstLoop) {
				
				for (int i = 0; i < wordVisited2Size; i++) {
					
					if (similarSynSetComparaison(wordVisited1List.get(wordVisited1Size-1), wordVisited2List.get(i)) != null) {
						similarSynsetPos1 = wordVisited1Size-1;
						similarSynsetPos2 = i;
						similarSynset = similarSynSetComparaison(wordVisited1List.get(wordVisited1Size-1), wordVisited2List.get(i));
						similarWords = true;
						break;
					}
				}
			}
			if (!similarWords && !SecondLoop) {
				for (int i = 0; i < wordVisited1Size; i++) {
					if (similarSynSetComparaison(wordVisited1List.get(i), wordVisited2List.get(wordVisited2Size-1)) != null) {
						similarSynsetPos1 = i;
						similarSynsetPos2 = wordVisited2Size-1;
						similarSynset = similarSynSetComparaison(wordVisited1List.get(i), wordVisited2List.get(wordVisited2Size-1));
						similarWords = true;
						break;
					}
				}
			}
			if (!similarWords) {
				if (!firstLoop) {
					Set<ISynset> hyperSetWordList1 = getHyperSynSet(wordVisited1List.get(wordVisited1Size-1));
					if (hyperSetWordList1.isEmpty()) { 
						firstLoop = true; 
					}
					else { 
						wordVisited1List.add(hyperSetWordList1); 
					}
				}
				if (!SecondLoop) {
					Set<ISynset> hyperSetWordList2 = getHyperSynSet(wordVisited2List.get(wordVisited2Size-1));
					if (hyperSetWordList2.isEmpty()) { 
						SecondLoop = true; 
					}
					else { 
						wordVisited2List.add(hyperSetWordList2); 
					}
				}
			}
		}
		
		if (similarSynset == null) { 
			return 0.0; 
		}

		int value1 = similarSynsetPos1;
		int value2 = similarSynsetPos2;
		int value3 = synsetSize(similarSynset);
		
		double result = 2*value3 / (double) (value1+value2+2*value3);
		
		return result;
	}
	
	private static int synsetSize(ISynset similarSynset) {
		
		//If no related synsets found return 0
		if (similarSynset.getRelatedSynsets(Pointer.HYPERNYM).isEmpty()) { 
			return 0; 
		}

		List<Set<ISynset>> synsetList = new ArrayList<>();		
		Set<ISynset> synset = new HashSet<>();
		
		synset.add(similarSynset);
		synsetList.add(synset);
		
		boolean synsetTop = false;
		
		int size = -1;
		
		while (!synsetTop) {
			
			Set<ISynset> nextSynset = new HashSet<>();
			
			for (ISynset s : synsetList.get(synsetList.size()-1)) {
				
				List<ISynsetID> hyperIDList = s.getRelatedSynsets(Pointer.HYPERNYM);
				
				if (!hyperIDList.isEmpty()) {
					for (ISynsetID hyperID : hyperIDList) { nextSynset.add(wordNetDictionary.getSynset(hyperID)); }
				} else {
					synsetTop = true;
					size = synsetList.size()-1;
					break;
				}
			}
			synsetList.add(nextSynset);
		}
		return size;
	}
	
	private static ISynset similarSynSetComparaison(Set<ISynset> synSet1, Set<ISynset> synSet2) {
		
		for (ISynset s2 : synSet2) {
			if (synSet1.contains(s2)) {
				return s2; 
			}
		}
		return null;
	}
	
	private static Set<ISynset> getHyperSynSet(Set<ISynset> set) {
		
		Set<ISynset> hyperSynSet = new HashSet<>();
		
		for (ISynset synset : set) {
			
			List<ISynsetID> hyperIDList = synset.getRelatedSynsets(Pointer.HYPERNYM);
			if (!hyperIDList.isEmpty()) {
				for (ISynsetID hyperID : hyperIDList) { hyperSynSet.add(wordNetDictionary.getSynset(hyperID)); }
			}
		}
		return hyperSynSet;
	}

	//Lexical Similarity Coefficient
	private static double findLexicalSimilarity(String word1, String word2) {
		
		Set<Character> set1 = new HashSet<>();
		Set<Character> set2 = new HashSet<>();
		Set<Character> similarSet = new HashSet<>();
		
		for (char element : word1.toCharArray()) {
			set1.add(element); 
		}
		for (char element : word2.toCharArray()) { 
			set2.add(element); 
		}
		for (char c : set1) {
			if (set2.contains(c)) { similarSet.add(c); }
		}
		double result = Math.sqrt(similarSet.size() / (double) (set1.size() + set2.size() + similarSet.size()));
		
		return result;
	}
	
	//Finds the maximum similarity coefficient
	public static double findSimilarity(String word1, String word2, WordNetSimilarity wordNet) {
		double similarity = Math.max(findSemanticSimilarity(word1, word2),findLexicalSimilarity(word1, word2));
		return similarity;
	}
	
}
