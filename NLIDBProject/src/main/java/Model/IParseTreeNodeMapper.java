package Model;

public interface IParseTreeNodeMapper extends Iterable<Node>  {


	public int size();
	
	public void removeUnknownNodes();
	
	public String toString();

	public boolean equals(Object obj);
	
	public Query queryTreeToSQL(SchemaGraph schema);
	
}

