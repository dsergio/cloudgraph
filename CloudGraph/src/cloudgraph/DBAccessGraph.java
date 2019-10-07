package cloudgraph;

import java.util.List;
import java.util.Map;

public interface DBAccessGraph {
	
	public void init() throws Exception;
	public void close();
	
	public void createTable(String tableName);
	public void addGraphVertex(String nodeId, String adj, String location, String graphName);
	public void updateItem(String graphName, String type, String edge);
	public Map<LocationItem, List<Edge>> getCloudVerticesAndEdges(String graphName);
	
	
	public int getNumAdds();
	public int getNumReads();
	public int getNumUpdates();
	public long getAddTime();
	public long getReadTime();
	public long getUpdateTime();
	
}
