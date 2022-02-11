package ComponentNodeMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Node;
import Model.NodeData;
import Model.SchemaGraph;
import Model.WordNetSimilarity;
import Model.NodeData.ScoreComparator;

public class NodeMapperParser {

	public Map<String, NodeData> mappingRules;
	
	public WordNetSimilarity wordnet;

	public NodeMapperParser() throws Exception {
		wordnet = new WordNetSimilarity();
		
		mappingRules = new HashMap<String, NodeData>();
		
		
		//We have here all the rules to map the nodes to the SQL query
		
		//Select Rule
		mappingRules.put("return", new NodeData("SN", "SELECT"));
		
		//Functions Rules
		mappingRules.put("total",     new NodeData("FN", "SUM"));
		mappingRules.put("number", new NodeData("FN","COUNT"));
		mappingRules.put("average",     new NodeData("FN", "AVG"));
		mappingRules.put("most",     new NodeData("FN", "MAX"));
		
		//Logical Rules
		mappingRules.put("and",    new NodeData("LN", "AND"));
		mappingRules.put("or",    new NodeData("LN", "OR"));

		//Operator Rules
		mappingRules.put("equal", new NodeData("ON", "="));
		mappingRules.put("in", new NodeData("ON", "="));
		mappingRules.put("by", new NodeData("ON", "="));
		mappingRules.put("on", new NodeData("ON", "="));
		mappingRules.put("containing", new NodeData("ON", "="));
		mappingRules.put("contain", new NodeData("ON", "="));
		mappingRules.put("not",    new NodeData("ON", "!="));
		mappingRules.put("more",    new NodeData("ON", ">"));
		mappingRules.put("less",    new NodeData("ON", "<"));
		mappingRules.put("greater",    new NodeData("ON", ">"));
		mappingRules.put("before", new NodeData("ON", "<"));
		mappingRules.put("after", new NodeData("ON", ">"));
		
		//Quantifier Rules
		mappingRules.put("any",    new NodeData("QN", "ANY"));
		mappingRules.put("each",    new NodeData("QN", "EACH"));
		mappingRules.put("all",    new NodeData("QN", "ALL"));


	}
	

	public List<NodeData> getNodeDataChoices(Node node, SchemaGraph schema) {
		
		//This function will get Node and generate possible choices for mapping
		
		List<NodeData> result = new ArrayList<NodeData>();
		
		//If node is root, no need to generate choices
		if (node.getWord().equals("ROOT")) {
			result.add(new NodeData("ROOT", "ROOT"));
			return result;
		}
	
		Set<NodeData> valueNodes = new HashSet<NodeData>();
		
		//Allow to split word and the tag with a "/"
		String wordSplit[] = node.getWord().toLowerCase().split("/");
		String word = wordSplit[0];
		node.setWord(wordSplit[0]);
		
		//Contain Rule Key Word
		//Check if the word is in the mapping rules
		if (mappingRules.containsKey(word)) {
			result.add(mappingRules.get(word));
			return result;
		}
		
		//Map Tables
		for (String tableName : schema.getTableNames()) {
			
			//After getting our schema, we calculate similarity between word and the tableName
			//This is done by library wordNet
			result.add(new NodeData("NN", tableName,WordNetSimilarity.findSimilarity(word, tableName, wordnet)));
			
			//Map Columns
			for (String columnName : schema.getColumns(tableName)) {
				
				//we verfiy now the similarity of the columns
				result.add(new NodeData("NN", tableName+"."+columnName,WordNetSimilarity.findSimilarity(word, columnName, wordnet)));
				
					/*
				//Map Values
				for (String value : schema.getValues(tableName, columnName)) {
					
					valueNodes.add(new NodeData("VN", tableName+"."+columnName, WordNetSimilarity.findSimilarity(word, value, wordnet)));
				}
				*/
				
			}
		}
		
		//We will return a collection with all our mapping found
		Collections.sort(result, new NodeData.ScoreComparator());

		return result;
		
	}
	

}
