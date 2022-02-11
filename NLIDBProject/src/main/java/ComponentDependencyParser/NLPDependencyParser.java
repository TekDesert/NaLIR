package ComponentDependencyParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;

public class NLPDependencyParser {
	
	private String text;
	MaxentTagger tagger;
	DependencyParser parser;

	public NLPDependencyParser(String text) {
		super();
		this.text = text;
	}

	public NLPParseTreeResult parseText() {
		
		//Sentence List will hold the word list 
		//Tag List will contain the tag of each words
		//GrammaticalStructure will contain the relation between each word (NLP logic to link words)
		List<HasWord> sentenceList = new ArrayList<HasWord>();
		List<TaggedWord> taggedWordList = new ArrayList<TaggedWord>();
		GrammaticalStructure grammaticalStructure = null;
		
		//Paths to parser and tagger dictionary to reference english words
		String NLPparserPath = DependencyParser.DEFAULT_MODEL;
		String NLPTaggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
		
		//tagger is a class that contains the tags of each word. Whether the word is a Name Node, Value Node ect..
		//we will give our tagger the path of the dictionary of tags that will be used
		MaxentTagger tagger;
		tagger = new MaxentTagger(NLPTaggerPath);
		DependencyParser parser = DependencyParser.loadFromModelFile(NLPparserPath);
		
		//To process the text, we give the text to the processing class
		//tokens will hold the words that are tokenized by the processor
		DocumentPreprocessor tokens = new DocumentPreprocessor(new StringReader(this.text));
		
		//each token word will have their tag set here
		// for each tag we will retrieve its gramatical structure (link between word)
		// a token can be more than one word
		for (List<HasWord> sentence : tokens) {
			taggedWordList = tagger.tagSentence(sentence);
			//predicts the relation between the tagged words
			grammaticalStructure = parser.predict(taggedWordList);
		}
		//we have successfully retrieved all the words with their relations and their tags
		//Once every word is mapped with their tag, we will return the result to our main pipeline class and build the parse tree
		NLPParseTreeResult NLPresult = new NLPParseTreeResult(taggedWordList, sentenceList,grammaticalStructure);
		
		
		return NLPresult;
		
	}
	
	

}
