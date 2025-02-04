package cloudgraph;
import org.json.simple.JSONObject;

public class LocationItem {
	
	private int x;
	private int y;
	private String type;
	
	public LocationItem(int x, int y, String type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public JSONObject getJson() {
		JSONObject obj = new JSONObject();
		obj.put("x", x);
		obj.put("y", y);
		obj.put("type", type);
		return obj;
	}
	
	@Override
	public String toString() {
		
		String str = "(" + x + ", " + y + ") " + type;
		
		return str;
	}
	
	static int area(LocationItem e1, LocationItem e2) {
		return Math.abs(e1.getX() - e2.getX()) * Math.abs(e1.getY() - e2.getY());
	}
}
