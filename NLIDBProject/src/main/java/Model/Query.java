package Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Query {

	private List<Query> sqlBlocks;
	private Map<String, Collection<String>> sqlMap;
	
	public Query() {
		
		sqlMap = new HashMap<>();
		
		sqlMap.put("SELECT", new ArrayList<String>());
		sqlMap.put("FROM", new HashSet<String>());
		sqlMap.put("WHERE", new HashSet<String>());
		
		sqlBlocks = new ArrayList<Query>();
	}

	
	public Collection<String> getCollection(String key) { 
		return sqlMap.get(key); 
	}
	
	public void addBlock(Query query) {
		sqlBlocks.add(query);
		sqlMap.get("FROM").add("BLOCK"+sqlBlocks.size());
	}

	private StringBuilder selectBlockToString(Collection<String> SELECT) {
		
		StringBuilder result = new StringBuilder();
		int iteration = 0;
		
		for (String element : SELECT) {

			if (element.length() == 0 || iteration == 0) {
				result.append(element);
				iteration++;
			} else {
				result.append(", ").append(element);
			}
		}
		
		return result;
	}

	private StringBuilder conditionBlockToString(Collection<String> WHERE) {
		
		StringBuilder result = new StringBuilder();
		
		for (String element : WHERE) {
			
			if (result.length() == 0) {
				result.append(element);
			} else {
				result.append(" AND ").append(element);
			}
		}
		return result;
	}

	
	@Override
	public String toString() {
		
		//Check if query contains SELECT or FROM clause
		if (sqlMap.get("SELECT").isEmpty() || sqlMap.get("FROM").isEmpty()) {
			return "Invalid Query"; 
		}
		
		StringBuilder result = new StringBuilder();
		
		//Return SQL Blocks

		for (int i = 0; i < sqlBlocks.size(); i++) {
			
			result.append("BLOCK"+(i+1)+":").append("\n");
			result.append(sqlBlocks.get(i).toString()).append("\n");
			result.append("\n");
		}
		
		result.append("SELECT ").append(selectBlockToString(sqlMap.get("SELECT"))).append("\n");
		result.append("FROM ").append(selectBlockToString(sqlMap.get("FROM")));
		
		if (!sqlMap.get("WHERE").isEmpty()) {
			result.append("\n");
			result.append("WHERE ").append(conditionBlockToString(sqlMap.get("WHERE")));
		}

		return result.toString();
		
	}
	
	public void addKeyValueToQuery(String key, String value) {
		sqlMap.get(key).add(value);
	}
	
}
