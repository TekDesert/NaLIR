package Model;

import java.util.Comparator;


public class NodeData implements Comparable<NodeData> {

		private String value;
		private String type; 

		private double NodeScore = 1.0;
		
		public NodeData(String type, String value) {
			this.value = value;
			this.type = type;
		}
		public NodeData(String type, String value, double score) {
			this(type, value);
			this.NodeScore = score;
		}
		public NodeData(NodeData data){
			this.value = data.value;
			this.type = data.type;
			this.NodeScore = data.NodeScore;
		}

		public String getType() { 
			return type; 
		}
		public String getValue() {
			return value;
		}
		
		public double getScore(){
			return NodeScore;
		}
		
		public void setScore(double score) {
			NodeScore = score;
		}
		public boolean NodeInSchema (NodeData node) {

			if ( value == null || type == null || node.getType() == null || node.getValue() == null) {
				return false;
			}

			int indexNodeinfo = node.getValue().indexOf('.');

			int index = value.indexOf('.');
			
			if (index == -1) {

				index = value.length();
			}

			if (indexNodeinfo == -1) {

				indexNodeinfo = node.getValue().length();
			}

			if (node.getValue().substring(0, indexNodeinfo - 1).equals(value.substring(0, index - 1))) {

				return true;
			}


			return false;
		}
		
		public boolean NodeEqualSchema (NodeData node) {

			if (value == null || type == null || node.getType() == null || node.getValue() == null) {
				return false;
			}

			if (type.equals(node.getType()) && value.equals(node.getValue())) {

				return true;
			}

			return false;
		}

		
		@Override
		public String toString() {
			return "Node info : " + type +": "+value;
		}
		
		@Override
		public int compareTo(NodeData n) {
			return this.getValue().compareTo(n.getValue());
		}
		
		public static class ScoreComparator implements Comparator<NodeData> {
			
			public int compare(NodeData node1, NodeData node2) {
				if (node1.NodeScore < node2.NodeScore) { 
					return 1; 
					}
				else if (node1.NodeScore > node2.NodeScore) {
					return -1; 
					}
				else { 
					return 0; 
					}
			}
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
			
			NodeData node = (NodeData) object;
			if (value == null) {
				
				if (node.value != null) {
					return false;
				}
			}
			else if (type == null) {
				
				if (node.type != null) {
					return false;
				}
			} else if (!type.equals(node.type)) {
				return false;
			}
			 else if (!value.equals(node.value)) {
				return false;
			}
			return true;
		}
		
}
