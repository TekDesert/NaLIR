package Model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchemaGraph {

	private Map<String, Map<String, String>> tables;

	private Map<String, Map<String, Set<String>>> rows;

	private Map<String, Set<String>> keys;

	private Map<String, Set<String>> join;

	public SchemaGraph(Connection dbConnection) throws SQLException {

		DatabaseMetaData meta = dbConnection.getMetaData();
		
		tables = new HashMap<>();
		rows = new HashMap<>();
		
		String[] types = {"TABLE"};
		
		ResultSet tableResultSet = meta.getTables(null, null, "%", types);
		
	    Statement statement = dbConnection.createStatement();
	    
		System.out.println("Loading schema graph");
	    
		//Retrieving Tables
		while (tableResultSet.next()) {
			
			System.out.println("Retrieving table : " + tableResultSet.getString("TABLE_NAME"));
			String tableName = tableResultSet.getString("TABLE_NAME");
			tables.put(tableName, new HashMap<>());
			rows.put(tableName, new HashMap<>());
			
			Map<String, String> table = tables.get(tableName);
			Map<String, Set<String>> rowsSet = rows.get(tableName);
			
			ResultSet columnResultSet = meta.getColumns(null, null, tableName, null);
			
			//Retrieving Columns
			while (columnResultSet.next()){

				String name = columnResultSet.getString("COLUMN_NAME");
				String type = columnResultSet.getString("TYPE_NAME");
				table.put(name, type); 
				
				String query = "SELECT " + name + " FROM " + tableName + " ORDER BY RAND() LIMIT 50;";
				ResultSet rowsResultSet = statement.executeQuery(query);
				
				rowsSet.put(name, new HashSet<String>());
				
				Set<String> valuesResultSet = rowsSet.get(name);
				
				while (rowsResultSet.next()){
					
					String value = rowsResultSet.getString(1);
					
					if (!rowsResultSet.wasNull()) {
						valuesResultSet.add(value);
					}
				}
				
			}			
		}
		
		if (statement != null) {
			statement.close(); 
		}
		
		findPrimaryKeys(meta);
		findJoins();
		System.out.println("Schema graph retrieved.");
	}

	private void findPrimaryKeys(DatabaseMetaData metadata) throws SQLException {
		
		keys = new HashMap<>();
		
		for (String name : tables.keySet()) {
			
			ResultSet primaryKeyResultSet = metadata.getPrimaryKeys(null, null, name);
			keys.put(name, new HashSet<String>());
			
		    while (primaryKeyResultSet.next()) {
		    	
		    	keys.get(name).add(primaryKeyResultSet.getString("COLUMN_NAME"));
		    	
		    }
		}
	}
	
	private void findJoins() {
		
		join = new HashMap<String, Set<String>>();
		
		for (String name : tables.keySet()) {
			
			join.put(name, new HashSet<String>());
			
		}
		
		for (String table1 : tables.keySet()) {
			
			for (String table2 : tables.keySet()) {
				
				if (table1.equals(table2)) { 
					
					continue; 
				}
				
				if (!findJoinKeys(table1, table2).isEmpty()) {
					
					join.get(table1).add(table2);
					join.get(table2).add(table1);
					
				}
			}
		}
	}

	public Set<String> findJoinKeys(String table1, String table2) {
		
		Set<String> keysTable1 = keys.get(table1);
		Set<String> keysTable2 = keys.get(table2);
		
		if (keysTable1.equals(keysTable2)) { 
			
			return new HashSet<String>(); 
			
		}
		
		boolean containSimilarKeys1 = true;
		
		for (String key : keysTable1) {
			if (!tables.get(table2).containsKey(key)) {
				
				containSimilarKeys1 = false;
				
				break;
			}
		}
		
		if (containSimilarKeys1) {
			
			return new HashSet<String>(keysTable1); 
			
		}
		
		boolean containSimilarKeys2 = true;
		
		for (String key : keysTable2) {
			if (!tables.get(table1).containsKey(key)) {
				containSimilarKeys2 = false;
				break;
			}
		}
		
		if (containSimilarKeys2) { 
			
			return new HashSet<String>(keysTable2); 
		}
		
		return new HashSet<String>();
	}
	

	public List<String> findSchemaJoin(String table1, String table2) {
		
		if (tables.containsKey(table1) == false || tables.containsKey(table2) == false) {
			
			return new ArrayList<String>();
			
		}
		
		HashMap<String, Boolean> visitedTableMap = new HashMap<>();
		
		for (String name : tables.keySet()) {
			
			visitedTableMap.put(name, false);
		}
		
		HashMap<String, String> oldTableMap = new HashMap<>();
		LinkedList<String> currentTableMap = new LinkedList<>();
		
		currentTableMap.addLast(table1);
		visitedTableMap.put(table1, true);
		
		boolean joinResult = false;
		
		while (!currentTableMap.isEmpty() && joinResult == false) {
			
			String table = currentTableMap.removeFirst();
			
			for (String currentTable : join.get(table)) {
				
				if (!visitedTableMap.get(currentTable)) {
					
					visitedTableMap.put(currentTable, true);
					currentTableMap.addLast(currentTable);
					oldTableMap.put(currentTable, table);
					
				}
				if (currentTable.equals(table2)) { 
					joinResult = true; 
				}
			}
		}

		LinkedList<String> result = new LinkedList<>();
		
		if (visitedTableMap.get(table2)) {
			
			String tableResult = table2;
			
			result.push(tableResult);
			
			while (oldTableMap.containsKey(tableResult)) {
				
				tableResult = oldTableMap.get(tableResult);
				result.push(tableResult);
				
			}
		}
		
		return result;
	}
	
	public Set<String> getTableNames() {
		return tables.keySet();
	}
	
	public Set<String> getColumns(String tableName) {
		return tables.get(tableName).keySet();
	}
	
	public Set<String> getValues(String tableName, String columnName){
		return rows.get(tableName).get(columnName);
	}

	@Override
	public String toString() {
		
		String result = "";
		
		for (String name : tables.keySet()) {
			
			result += "table: "+name+"\n";
			result += "{";
			
			Map<String, String> columns = tables.get(name);
			
			for (String column : columns.keySet()) {
				
				result += column+": "+columns.get(column)+"\t";
				
			}
			
			result += "}\n\n";
		}
		
		return result;
	}


}
