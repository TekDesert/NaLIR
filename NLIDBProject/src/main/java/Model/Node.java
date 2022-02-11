package Model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Node implements Comparable<Node> {

	private int index = 0;

	public String tag;
	
	public NodeData data = null;

	public String word;

	public Node parent = null;

	public List<Node> children = new ArrayList<Node>();
	
	public boolean invalid = false;
	
	public Node(int index, String word, String tag){
		this(index, word, tag, null);
	}
	
	public Node(int index, String word, String posTag, NodeData data) {
		this.index = index;
		this.word = word;
		this.tag = posTag;
		this.data = data;
	}
	
	public Node(String word, String posTag, NodeData data) {
		this(0, word, posTag, data);
		
	}

	public NodeData getData() {
		return data; 
	}
	
	public void setInfo(NodeData data) {
		this.data = data; 
		
	}
	
	public String getWord() { 
		return word; 
		
	}
	
	public void setWord(String word) {
		this.word = word;
		
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getTag() { 
		return tag; 
		
	}
	
	public List<Node> getChildren() { 
		return children;
		
	}
	
	public void setChild(Node child) {
		this.children.add(child);
		
	}
	
	public Node getParent() {
		return parent;
		
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
		
	}
	
	public void removeChildren (Node child) {

		for (int i = 0; i < children.size(); i ++) {

			if (children.get(i).equals(child)) {

				children.remove(i);
				return;
			}
		}
	}
	
	public Node copy(Node node){
		
		if (node == null) {
			return null;
		}
		
		Node nodeCopy = new Node(node.index, node.word, node.tag, node.data);
		
		for (Node child : node.children){
			Node copyChild = copy(child);
			copyChild.parent = nodeCopy;
			nodeCopy.children.add(copyChild);
		}
		
		return nodeCopy;
	}

	public Node[] getNodesArray() {
		List<Node> nodesList = new ArrayList();
		LinkedList<Node> linkedNodeList = new LinkedList();
		linkedNodeList.push(this);
		
		while (!linkedNodeList.isEmpty()) {
			Node node = linkedNodeList.pop();
			nodesList.add(node);
			List<Node> currChildren = node.getChildren();
			for (int i = currChildren.size()-1; i >= 0; i--) {
				linkedNodeList.push(currChildren.get(i));	
			}
		}
		int N = nodesList.size();
		Node[] nodes = new Node[N];
		for (int i = 0; i < N; i++) {
			nodes[i] = nodesList.get(i);
		}
		return nodes;
	}
	
	
	@Override
	public int compareTo(Node n) {
		return Integer.compare(this.getIndex(), n.getIndex());
	}

	public String toString() {
		String result = "("+index+")"+word;
		if (data != null) {
			result += "("+data.getType()+":"+data.getValue()+")";
		}
		return result;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	@Override
	public boolean equals(Object object) {
		
		
		if (this == object) { 
			return true; 
		}
		
		if (object == null) { 
			return false; 
		}
		
		if (getClass() != object.getClass()) { 
			return false; 
		}
		
		Node node = (Node) object;
		
		if (index != node.index) { 
			return false; 
		}
		
		if (!word.equals(node.word)) { 
			return false; 
		}
		
		if (!tag.equals(node.tag)) { 
			return false; 
		}
		
		if (data != node.data) {
			
			if (data == null || node.data== null) { 
				return false; 
			}
			
			if (!data.equals(node.data)) { 
				return false; 
			}
		}
		
		if (children != node.children) {
			
			if (children == null || node.children == null) { 
				return false; 
			}
			
			if (children.size() != node.children.size()) {
				return false; 
			}
			
			for (int i = 0; i < children.size(); i++) {
				
				if (!children.get(i).equals(node.children.get(i))) {
					return false; 
				}	
			}
		}
		return true;
	}

	

}
