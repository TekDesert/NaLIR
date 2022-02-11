package Model;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ComponentQueryTranslator.QueryTreeTranslator;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class ParseTreeNodeMapper implements IParseTreeNodeMapper {

	Node root;
	
	private Node[] nodes;

	public ParseTreeNodeMapper() { }
	
	public ParseTreeNodeMapper(List<TaggedWord> tagged, List<HasWord> sentence, GrammaticalStructure gs) {
		
				int N = tagged.size()+1;
				nodes = new Node[N];
				root = new Node(0, "ROOT", "ROOT");
				nodes[0] = root;
				
				//NLP word mapping
				for (int i = 0; i < N-1; i++) {
					if(tagged.get(i).toString().contains("!")) {
						String word = tagged.get(i).toString().replace("!", " ");
						nodes[i+1] = new Node(i+1, word, tagged.get(i).tag());
					}
					else {
						nodes[i+1] = new Node(i+1, tagged.get(i).toString(), tagged.get(i).tag());
					}
				}
				
				//NLP parse tree build
				for (TypedDependency typedDep : gs.allTypedDependencies()) {
					int from = typedDep.gov().index();
					int to   = typedDep.dep().index();
					nodes[to].parent = nodes[from];
					nodes[from].children.add(nodes[to]);
					
				}
				
				
	}
	
	
	public Node[] getNodes() {
		return this.nodes;
	}

	public ParseTreeNodeMapper(Node node) {
		root = node.copy(node);
	}
	public ParseTreeNodeMapper(ParseTreeNodeMapper other) {
		this(other.root);
	}
	
	@Override
	public int size() {
		return root.getNodesArray().length;
	}

	private void removeUnknownNodes(Node node) {
		
		if (node == null) { 
			return; 
		}
		
		List<Node> nodeChildren = new ArrayList(node.getChildren());
		
		for (Node child : nodeChildren) {
			removeUnknownNodes(child);
		}

		if (node != root && node.getData().getType().equals("UNKNOWN")) {
			
			node.parent.getChildren().remove(node);
			
			for (Node child : node.getChildren()) {
				
				node.parent.getChildren().add(child);
				child.parent = node.parent;
				
			}	
		}

	}
	
	@Override
	public void removeUnknownNodes() {
		if (root.getChildren().get(0).getData() != null) {
			removeUnknownNodes(root);
		}
	}
	
	@Override
	public Query queryTreeToSQL(SchemaGraph schema) {
		QueryTreeTranslator translator = new QueryTreeTranslator(root, schema);
		return translator.getResult(); 
	}

	@Override
	public boolean equals(Object object) {
		
		if (object == null) {
			return false;
		}
		
		if (this == object) {
			return true;
		}

		if (getClass() != object.getClass()) {
			return false;
		}
		
		ParseTreeNodeMapper parseTree = (ParseTreeNodeMapper) object;
		
		if (root == null) {
			if (parseTree.root != null) {
				return false;
			}
		} else if (!root.equals(parseTree.root)) {
			return false;
		}
		
		return true;
	}

	public Node[] getNodesArray() {
		return root.getNodesArray();
	}
	
	public class ParseTreeNodeMapperIterator implements Iterator<Node> {
		
		LinkedList<Node> nodeList = new LinkedList<>();
		
		ParseTreeNodeMapperIterator() {
			nodeList.push(root);
		}
		
		@Override
		public boolean hasNext() {
			return !nodeList.isEmpty(); 
		}
		@Override
		public Node next() {
			
			Node node = nodeList.pop();
			List<Node> children = node.getChildren();
			
			for (int i = children.size()-1; i >= 0; i--) {
				nodeList.push(children.get(i));
			}
			return node;
		}
	}
	
	@Override
	public ParseTreeNodeMapperIterator iterator() { 
		return new ParseTreeNodeMapperIterator(); 
	}
	
	public String getSentence() {
		
		boolean firstNode = true;
		
		StringBuilder result = new StringBuilder();
		
		for (Node node : this) {
			if (firstNode) {
				result.append(node.getWord());
				firstNode = false;
			} else {
				result.append(" ").append(node.getWord());
			}
		}
		return result.toString();
	}
	
	private String nodeToString(Node node) {
		
		if (node == null) { 
			return ""; 
		}
		
		String result = node.toString() + " -> ";
		result += node.getChildren().toString() + "\n";
		
		for (Node child : node.getChildren()) {
			result += nodeToString(child);
		}
		
		return result;
		
	}
	
	@Override
	public String toString() {
		
		StringBuilder result = new StringBuilder();
		
		result.append("Sentence: ").append(getSentence()).append("\n");
		
		result.append(nodeToString(root));
		
		return result.toString();
		
	}

	
	
}
