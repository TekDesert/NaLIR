package ComponentStructureAdjustor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import Model.Node;
import Model.NodeData;
import Model.ParseTreeNodeMapper;

public class StructureAdjustor {
	
	public static List<ParseTreeNodeMapper> adjust(ParseTreeNodeMapper parseTree) { 
		Set<ParseTreeNodeMapper> parseTreeList = new HashSet<ParseTreeNodeMapper>();
		for (Node node : parseTree) {
			System.out.println("Adjust Node : " + node.toString());
			parseTreeList.addAll(adjustNode(parseTree, node));
		}
		return new ArrayList<ParseTreeNodeMapper>(parseTreeList);
	}
	
	private static Set<ParseTreeNodeMapper> adjustNode(ParseTreeNodeMapper parseTree, Node node) {
		
		
		
		Set<ParseTreeNodeMapper> adjustedParseTree = new HashSet<>();
		
		//This function verify if parent node is not null
		if (node.parent == null) { 
			
			return adjustedParseTree; 
			
		}
		
		//Move the parent node with the  childrens node
		for (Node childrens : node.parent.getChildren()) {
			
			if (childrens == node) { 
				continue; 
			}
			
			ParseTreeNodeMapper newParseTree = new ParseTreeNodeMapper(parseTree);
			System.out.println("Move Child Nodes");
			System.out.println(node.toString());
			System.out.println(childrens);
			moveChildNodes(searchNode(newParseTree, node), searchNode(newParseTree, childrens));
			if(newParseTree.getNodesArray()[1].getWord().equals("return")) {
				adjustedParseTree.add(newParseTree);
			}
			
		}
		
		//Take all node children with the child
		for (Node child : node.getChildren()) {
			
			ParseTreeNodeMapper newParseTree = new ParseTreeNodeMapper(parseTree);
			moveNodes(searchNode(newParseTree, node), searchNode(newParseTree, child));
			if(newParseTree.getNodesArray()[1].getWord().equals("return")) {
				adjustedParseTree.add(newParseTree);
			}
			
		}

		// If we have more than 2 children, we will for each children move the node
		if (node.getChildren().size() >= 2) {
			
			List<Node> children = node.getChildren();
			
			for (int i = 1; i < children.size(); i++) {
				
				ParseTreeNodeMapper newParseTree = new ParseTreeNodeMapper(parseTree);
				moveNodes(searchNode(newParseTree, children.get(0)), searchNode(newParseTree, children.get(i)));
				if(newParseTree.getNodesArray()[1].getWord().equals("return")) {
					//Adjust parse tree suggestion
					adjustedParseTree.add(newParseTree);
				}
			}
		}
		
		
		for (Node child : node.getChildren()) {
			
			ParseTreeNodeMapper newParseTree = new ParseTreeNodeMapper(parseTree);
			moveChildrenNodes(searchNode(newParseTree, node), searchNode(newParseTree, child));
			if(newParseTree.getNodesArray()[1].getWord().equals("return")) {
				adjustedParseTree.add(newParseTree);
			}
			
		}
	
		return adjustedParseTree;
	}
	
	public static Node searchNode(ParseTreeNodeMapper parseTree, Node searchNode) {
		
		for (Node node : parseTree) {
			
			if (node.equals(searchNode)) { 
				return node; 
			}
			
		}
		
		return null;
	}

	public static void moveNodes(Node parent, Node child) {
		
		//Move each nodes with their corresponding word, tag...
		
		String word = child.word;
		String tag = child.tag;
		NodeData data = child.data;
		
		child.data = parent.data;
		child.word = parent.word;
		child.tag = parent.tag;
		
		parent.data = data;
		parent.word = word;
		parent.tag = tag;
		
	}
	
	private static void moveChildrenNodes(Node childrenNode, Node child) {
		
		//Move all Children nodes for each child node
		
		List<Node> children = childrenNode.getChildren();
		
		childrenNode.children = new ArrayList<Node>();
		
		for (Node node : children) {
			
			if (node != child) { 
				childrenNode.getChildren().add(node); 
			}
			
		}
		
		childrenNode.parent.children.add(child);
		
		child.parent = childrenNode.parent;
	}

	private static void moveChildNodes(Node childNode, Node children) {
		
		//Move individual child node
		
		List<Node> childrens = childNode.parent.children;
		
		childNode.parent.children = new ArrayList<Node>();
		
		for (Node node : childrens) {
			
			if (node != children) {
				childNode.parent.children.add(node);
			}
		}
		
		childNode.children.add(children);
		children.parent = childNode;
	}

}
