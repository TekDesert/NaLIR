package ComponentDependencyParser;

import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;

public class NLPParseTreeResult {
	
	List<TaggedWord> tagged;
	List<HasWord> sentence;
	GrammaticalStructure grammaticalStructure;
	
	public NLPParseTreeResult(List<TaggedWord> tagged, List<HasWord> sentence, GrammaticalStructure grammaticalStructure) {
		super();
		this.tagged = tagged;
		this.sentence = sentence;
		this.grammaticalStructure = grammaticalStructure;
	}

	public List<TaggedWord> getTagged() {
		return tagged;
	}

	public void setTagged(List<TaggedWord> tagged) {
		this.tagged = tagged;
	}

	public List<HasWord> getSentence() {
		return sentence;
	}

	public void setSentence(List<HasWord> sentence) {
		this.sentence = sentence;
	}

	public GrammaticalStructure getGrammaticalStructure() {
		return grammaticalStructure;
	}

	public void setGs(GrammaticalStructure grammaticalStructure) {
		this.grammaticalStructure = grammaticalStructure;
	}
	
	
	
	

}
