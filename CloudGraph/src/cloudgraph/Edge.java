package cloudgraph;

import org.json.simple.JSONObject;

public class Edge {
	
	private LocationItem v1;
	private LocationItem v2;
	private int weight;
	
	public Edge(LocationItem v1, LocationItem v2, int weight) {
		this.v1 = v1;
		this.v2 = v2;
		this.weight = weight;
	}
	
	public LocationItem v1() {
		return v1;
	}
	
	public LocationItem v2() {
		return v2;
	}
	
	public int weight() {
		return weight;
	}
	
	public String toString() {
		return "" + v1.toString() + "-" + v2.toString();
	}
	
	public JSONObject getJson() {
		JSONObject obj = new JSONObject();
		obj.put("v1", v1);
		obj.put("v2", v2);
		obj.put("weight", weight);
		return obj;
	}

}
