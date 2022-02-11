package ComponentQueryTranslator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.xalan.xsltc.compiler.sym;

import Model.Node;
import Model.Query;
import Model.SchemaGraph;
import jdk.nashorn.internal.runtime.ListAdapter;

public class QueryTreeTranslator {

	private Query query;
	private SchemaGraph schema;
	private int numberBlock = 1;
	
	public QueryTreeTranslator(Node root, SchemaGraph schema) {
		this(root, schema, false);
	}
	
	public QueryTreeTranslator(Node root, SchemaGraph schema, boolean queryBlock) {
		
		if (!queryBlock) {
			
			this.schema = schema;
			query = new Query();
			//will translate for the root node with children node
			translateSELECTBlock(root.getChildren().get(0));
			
			//Complex Query Tree has 2 or more blocks
			if (root.getChildren().size() >= 2) {
				translateComplexSQLQuery(root.getChildren().get(1));
			}
			
			if (schema != null) { 
				addJoinToQuery();
			}
		} else {
			this.schema = schema;
			query = new Query();
			translateQueryBlock(root);
		}
	}
	
	public Query getResult() {
		return query; 
	} 
	
	
	private static boolean containNumber(String nodeValue) {
		
	    int size = nodeValue.length();
	    
	    if (size == 0) {
	    	return false; 
	    }
	    
	    int i = 0;
	    
	    if (nodeValue.charAt(0) == '-') {
	    	
	        if (size == 1) { 
	        	return false; 
	        }
	        i = 1;
	    }
	    
	    for (; i < size; i++) {
	    	
	        char character = nodeValue.charAt(i);
	        
	        if (character != '.' && character < '0' || character > '9') { 
	        	return false; 
	        }
	    }
	    return true;
	}
	
	private void translateCondition(Node node) {
		
		String column = "ATTRIBUTE";
		String value = "VALUE";
		String compareOperator = "=";
		
		if (node.getData().getType().equals("VN")) {
			
			column = node.getData().getValue();
			value = node.getWord();
			
		} else if (node.getData().getType().equals("ON")) {
			
			compareOperator = node.getData().getValue();
			
			if(!node.getChildren().isEmpty()) {
				
				Node valueNode = node.getChildren().get(0);
				column = valueNode.getData().getValue();
				value = valueNode.getWord();
			}
		
		}
		
		if (!containNumber(value)) {
			value = "\""+value+"\""; 
		}
		
		query.addKeyValueToQuery("WHERE", column+" "+compareOperator+" "+value);
		query.addKeyValueToQuery("FROM", column.split("\\.")[0]);
	}

	private void translateSubQueryNode(Node node) {
		translateSubQueryNode(node, "");
	}
	private void translateSubQueryNode(Node node, String value) {
		
		if (!node.getData().getType().equals("NN")) { 
			return; 
		}
		
		if (!value.equals("")) {
			query.addKeyValueToQuery("SELECT", value+"("+node.getData().getValue()+")");
		} else {
			query.addKeyValueToQuery("SELECT", node.getData().getValue());
		}
		query.addKeyValueToQuery("FROM", node.getData().getValue().split("\\.")[0]);		
	}
	
	private void translateSubQueryBlock(Node node) {
		translateSubQueryBlock(node, "");
	}
	private void translateSubQueryBlock(Node node, String value) {
		
		translateSubQueryNode(node, value);
		
		for (Node child : node.getChildren()) {
			if (child.getData().getType().equals("NN")) {
				translateSubQueryNode(child);
			} else if (child.getData().getType().equals("ON") || child.getData().getType().equals("VN")){
				translateCondition(child);
			}
		}
	}
	
	private void translateQueryBlock(Node node) {
		//Translate every child with SQL equivalent 
		if (node.getData().getType().equals("FN")) {
			
			//use FN translation
			if (node.getChildren().isEmpty()) { 
				return; 
			}

			translateSubQueryBlock(node.getChildren().get(0), node.getData().getValue());
			
		} 
		else if (node.getData().getType().equals("NN")) {
			
			//Use NN translation
			translateSubQueryBlock(node);
			
		} else if (node.getData().getType().equals("ON") || node.getData().getType().equals("VN")){
			
			translateCondition(node);
	}
}
	
	private void translateComplexSQLQuery(Node node) {
		
		if (!node.getData().getType().equals("ON")) { 
			return; 
		}
		if (node.getChildren().size() != 2) { 
			return; 
		}
		
		QueryTreeTranslator leftQueryBlock = new QueryTreeTranslator(node.getChildren().get(0), schema, true);
		QueryTreeTranslator rightQueryBlock = new QueryTreeTranslator(node.getChildren().get(1), schema, true);
		
		query.addBlock(leftQueryBlock.getResult());
		query.addBlock(rightQueryBlock.getResult());
		
		query.addKeyValueToQuery("WHERE", "BLOCK"+(numberBlock++)+" "+node.getData().getValue()+" "+"BLOCK"+(numberBlock++));
	}
	
	private void translateSELECTBlock(Node node) {
		
		//This will take the root node and translate query block for every child node
		if (!node.getData().getType().equals("SN")) { 
			return; 
		}
		
		if(!node.getChildren().isEmpty()) {
			translateQueryBlock(node.getChildren().get(0));
		}
	}
	
	private void addJoinTables(List<String> joinPath) {
		
		for (int i = 0; i < joinPath.size()-1; i++) {
			
			Set<String> joinKeys = schema.findJoinKeys(joinPath.get(i), joinPath.get(i+1));
			for (String joinKey : joinKeys) {
				query.addKeyValueToQuery("WHERE", joinPath.get(i)+"."+joinKey+" = "+joinPath.get(i+1)+"."+joinKey);
			}
			
		}
	}
	
	private void addJoinToQuery() {
		
		List<String> fromTables = new ArrayList<String>(query.getCollection("FROM"));
		
		if (fromTables.size() <= 1) { 
			return; 
		}
		
		for (int i = 0; i < fromTables.size()-1; i++) {
			
			for (int j = i+1; j < fromTables.size(); j++) {
				
				List<String> joinTables = schema.findSchemaJoin(fromTables.get(i), fromTables.get(j));
				
				addJoinTables(joinTables);
				
			}
		}
	}
	
	public static void runQuery(Connection connection, String query, int maxNumberRows) {
		
		System.out.println("Run Query");
		String resultSelect = query.split("FROM")[0];
		String resultFrom = query.split("FROM")[1];
		String resultQuery = "";
		
		String[] resultSelectElements = resultSelect.split(" ");
		boolean containsColumns = false;
		boolean containsFunctions = false;
		
		System.out.println("Query before : " + query);
		
		for (int i = 1; i < resultSelectElements.length; i++) {
			//Function
			if(resultSelectElements[i].contains("(") && !resultSelectElements[i].contains(".")) {
				
				//One function
				if(!resultSelectElements[i].split("\\(")[1].contains("(")) {
				 String function = resultSelectElements[i].split("\\(")[0];
				 resultSelectElements[i] = " " + function + "(*)" + resultSelectElements[i].split("\\(")[1].split("\\)")[1];
				 containsFunctions = true;
				}
				//Two functions
				else {
					String functions = resultSelectElements[i].split("\\(")[1].split("\\(")[0];
					 resultSelectElements[i] = " " + functions + "(*)" + resultSelectElements[i].split("\\(")[1].split("\\)")[1];
					 containsFunctions = true;
				}
			}
			//all
			else if(!resultSelectElements[i].contains(".") && !resultSelectElements[i].contains("(")) {
				resultSelectElements[i]= "";
			}
			//Column
			else {
				containsColumns = true;
			}
		}
		
		if(containsColumns == false && containsFunctions == false) {
			resultSelectElements[1] = " * ";
		}

		for (int i = 0; i <resultSelectElements.length; i++) {
			resultQuery += resultSelectElements[i] + " ";
		}
		
		
		String[] joinTables = resultFrom.split("WHERE")[0].split(",");
		
		List<String> whereAttributeList = new ArrayList<String>();
		
		//Search for joins in where clause
		if(resultFrom.contains("WHERE")) {
			if(resultFrom.split("WHERE")[1].contains("AND")) {
				String[] whereAttributeArray = resultFrom.split("WHERE")[1].split("AND");
				for (int i = 0; i < whereAttributeArray.length; i++) {
					String attribute = whereAttributeArray[i].split("=")[0];
					whereAttributeList.add(attribute.split("[.]")[0]);
				}
			}
			if(resultFrom.split("WHERE")[1].contains("OR")){
				String[] whereAttributeArray = resultFrom.split("WHERE")[1].split("OR");
				for (int i = 0; i < whereAttributeArray.length; i++) {
					String attribute = whereAttributeArray[i].split("=")[0];
					whereAttributeList.add(attribute.split("[.]")[0]);
				}
			}
			
			else if(!resultFrom.split("WHERE")[1].contains("AND") && resultFrom.split("WHERE")[1].contains("OR")){
				whereAttributeList.add(resultFrom.split("WHERE")[1].split("=")[0].split("[.]")[0]);
			}
		}
		

		for (int i = 0; i < joinTables.length; i++) {
			System.out.println(joinTables[i]);
		}
		
		String joins = resultFrom.split("WHERE")[0];
		String where = "";
		
		if(resultFrom.contains("WHERE")) {
			where = " WHERE " + resultFrom.split("WHERE")[1];
		}
		
		List<String> joinList = new ArrayList<String>();
		
		for (int i = 0; i < joinTables.length; i++) {
			joinTables[i] = joinTables[i].replace("\n", "");
			joinList.add(joinTables[i]);
		}
		
		for (int i = 0; i < whereAttributeList.size(); i++) {

			if(!joinList.contains(whereAttributeList.get(i))) {
				joins += " JOIN " + whereAttributeList.get(i);
				joinList.add(whereAttributeList.get(i));
			}
			
		}
		
		if(joins.contains(",")) {
			joins = joins.replace(",", " JOIN");
		}

		
		resultQuery += "FROM" + joins + where + " LIMIT " + maxNumberRows + ";";
		
		System.out.println("Run query result : " + resultQuery);
		
		
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(resultQuery);
			
			ResultSetMetaData rsMetaData = rs.getMetaData();
			int columnsNumber = rsMetaData.getColumnCount();
			boolean resultFound = false;
			
			System.out.println("");
			System.out.println("Query Results");
			
        while(rs.next()){
        	
        	 for(int i = 1; i < columnsNumber+1; i++) {
        	        System.out.print(rs.getString(i) + " ");
        	 }
        	 System.out.println();
        	 resultFound = true;
        }
        
        if(resultFound == false) {
        	System.out.println("No Result Found");
        }
        
     } catch (SQLException e) {
        e.printStackTrace();
     }
		
		

	}

	
}
