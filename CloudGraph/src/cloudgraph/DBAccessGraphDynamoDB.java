package cloudgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.dynamodbv2.util.TableUtils;



/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class DBAccessGraphDynamoDB implements DBAccessGraph {

	/*
	 * Before running the code: Fill in your AWS access credentials in the provided
	 * credentials file template, and be sure to move the file to the default
	 * location (~/.aws/credentials) where the sample code will load the credentials
	 * from. https://console.aws.amazon.com/iam/home?#security_credential
	 *
	 * WARNING: To avoid accidental leakage of your credentials, DO NOT keep the
	 * credentials file in your source directory.
	 */

	/*
	 * Before running the code: Fill in your AWS access credentials in the provided
	 * credentials file template, and be sure to move the file to the default
	 * location (~/.aws/credentials) where the sample code will load the credentials
	 * from. https://console.aws.amazon.com/iam/home?#security_credential
	 *
	 * WARNING: To avoid accidental leakage of your credentials, DO NOT keep the
	 * credentials file in your source directory.
	 */
	
	private int numReads = 0;
	private int numAdds = 0;
	private int numUpdates = 0;
	
	private long readTime = 0;
	private long addTime = 0;
	private long updateTime = 0;

	private AmazonDynamoDB dynamoDB;
	private boolean dynamoLog = true;;

	public DBAccessGraphDynamoDB() {
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		dynamoLog = false;
	}

	/**
	 * The only information needed to create a client are security credentials
	 * consisting of the AWS Access Key ID and Secret Access Key. All other
	 * configuration, such as the service endpoints, are performed automatically.
	 * Client parameters, such as proxies, can be specified in an optional
	 * ClientConfiguration object when constructing a client.
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.ProfilesConfigFile
	 * @see com.amazonaws.ClientConfiguration
	 */
	@Override
	public void init() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default] credential profile
		 * by reading from the credentials file located at (~/.aws/credentials) for
		 * Linux and Mac machines.
		 */
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}
		dynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-west-2")
				.build();

	}

	public void createTable(String name) {
		try {
			String tableName = name;

			// Create a table with a primary hash key named 'name', which holds a string
			CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
					.withKeySchema(new KeySchemaElement().withAttributeName("nodeId").withKeyType(KeyType.HASH))
					.withAttributeDefinitions(new AttributeDefinition().withAttributeName("nodeId")
							.withAttributeType(ScalarAttributeType.S))
					.withProvisionedThroughput(
							new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(5L));

			// Create table if it does not exist yet
			TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
			// wait for the table to move into ACTIVE state
			try {
				TableUtils.waitUntilActive(dynamoDB, tableName);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Describe our new table
			DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
			TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
			System.out.println("Table Description: " + tableDescription);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
	
	public void addGraphVertex(String nodeId, String adj, String location, String graphName) {
		Map<String, AttributeValue> item = newNode(nodeId, adj, location);
		addItemToTable(item, graphName);
	}

	public void addItemToTable(Map<String, AttributeValue> item, String tableName) {
		try {

			// Add an item
			long time = System.currentTimeMillis();
			
			PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
			PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
			
			if (dynamoLog) {
				System.out.println(" -> DynamoDB ADD " + item.values().toString());
				System.out.println("..." + putItemResult);
			}
			numAdds++;
			
			addTime += (System.currentTimeMillis() - time);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
	
	public void updateItem(String tableName, String nodeId, String edge) {
		try {

			// Update item
//			UpdateItemRequest updateItemRequest = new UpdateItemRequest(tableName, item, null);
			
			long time = System.currentTimeMillis();
			Map<String,AttributeValue> key = new HashMap<>();
		    key.put("nodeId",new AttributeValue().withS(nodeId));
		    
			UpdateItemRequest updateItemRequest = new UpdateItemRequest()
			        .withTableName(tableName)
			        .withKey(key);
			        		
    		Map<String, AttributeValueUpdate> map = new HashMap<>();
    		if (edge != null) {
    			map.put("adj", new AttributeValueUpdate(new AttributeValue(edge),"PUT"));
    		}
    		
	        
	        updateItemRequest.setAttributeUpdates(map);
			
			UpdateItemResult updateItemResult = dynamoDB.updateItem(updateItemRequest);
			
			if (dynamoLog ) {
				System.out.println(" -> DynamoDB UPDATE " + nodeId);
				System.out.println("..." + updateItemResult);
			}
			numUpdates++;
			updateTime += (System.currentTimeMillis() - time);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
	
	public List<Edge> getCloudVertexEdges(String graphName, String nodeId) {
		
		JSONParser parser;
		Object obj;
		String adj = null;
		
		Map<String, AttributeValue> cloudNode = getNode(graphName, nodeId);
		
		if (cloudNode == null) {
			return null;
		} else {
			if (cloudNode.get("adj") != null) {
				adj = cloudNode.get("adj").getS();
			}
		}
		
		parser = new JSONParser();
		
		List<Edge> list = new ArrayList<Edge>();
		
		try {
			if (adj != null) {
				
				obj = parser.parse(adj);
				JSONArray arr = (JSONArray) obj;
				for (int i = 0; i < arr.size(); i++) {
					JSONObject row = (JSONObject) arr.get(i);
					
//					LocationItem item = new LocationItem(Integer.parseInt(row.get("x").toString()), Integer.parseInt(row.get("y").toString()), row.get("type").toString());
//					newNode.locationItems.add(item);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	
	public Map<String, AttributeValue> getNode(String tableName, String nodeId) {
		try {

			long time = System.currentTimeMillis();
			HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
			Condition condition = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
					.withAttributeValueList(new AttributeValue().withS(nodeId));
			
			scanFilter.put("nodeId", condition);
			ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
			ScanResult scanResult = dynamoDB.scan(scanRequest);
			
			if (dynamoLog) {
				System.out.println(" -> DynamoDB getNode('" + nodeId + "')...");
				System.out.println("..." + scanResult.toString());
			}
			numReads++;
			
			readTime += (System.currentTimeMillis() - time);
			
			if (scanResult.getCount() == 1) {
				return scanResult.getItems().get(0);
			} else {
				return null;
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
		
		return null;
	}
	
	public Map<LocationItem, List<Edge>> getCloudVerticesAndEdges(String graphName) {
		
		JSONParser parser;
		JSONObject obj;
		String location = null;
		
		
		List<Map<String, AttributeValue>> cloudNodes = getAllNodes(graphName);
		
		List<LocationItem> locations = new ArrayList<LocationItem>();
		Map<LocationItem, List<Edge>> pointsAndEdges = new HashMap<LocationItem, List<Edge>>();
		
		for (Map<String, AttributeValue> cloudNode : cloudNodes) {
			
			String adj = null;
			
			if (cloudNode == null) {
				return null;
			} else {
				if (cloudNode.get("location") != null) {
					location = cloudNode.get("location").getS();
				}
				if (cloudNode.get("adj") != null) {
					adj = cloudNode.get("adj").getS();
				}
			}
			
			parser = new JSONParser();
			
			try {
				if (location != null) {
					
					obj = (JSONObject)parser.parse(location);
					System.out.println("location: ...." + location);
					
					LocationItem item = new LocationItem(Integer.parseInt(obj.get("x").toString()), Integer.parseInt(obj.get("y").toString()), obj.get("type").toString());
					locations.add(item);
					if (!pointsAndEdges.containsKey(item)) {
						pointsAndEdges.put(item, new ArrayList<Edge>());
					}
					
					if (adj != null) {
						
						parser = new JSONParser();
						
						Object obj2 = parser.parse(adj);
						
						JSONArray arr = (JSONArray) obj2;
						
						for (int i = 0; i < arr.size(); i++) {
							System.out.println("tring to get " + arr.get(i));
							JSONObject row = (JSONObject) arr.get(i);
							

							
							String v1str = row.get("v1").toString();
							String v2str = row.get("v2").toString();
							int w = Integer.parseInt(row.get("weight").toString());
							
							JSONObject v1Obj = (JSONObject)parser.parse(v1str);
							JSONObject v2Obj = (JSONObject)parser.parse(v2str);
							
							LocationItem v1 = new LocationItem(Integer.parseInt(v1Obj.get("x").toString()), Integer.parseInt(v1Obj.get("y").toString()), v1Obj.get("type").toString());
							LocationItem v2 = new LocationItem(Integer.parseInt(v2Obj.get("x").toString()), Integer.parseInt(v2Obj.get("y").toString()), v2Obj.get("type").toString());
							
							System.out.println("v1str: " + v1str + " v2str: " + v2str + " weight: " + w);
							
							Edge e = new Edge(v1, v2, w);
							pointsAndEdges.get(item).add(e);
						}
						
					}
					
				}
				

				
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		return pointsAndEdges;
	}
	
	public List<Map<String, AttributeValue>> getAllNodes(String tableName) {
		try {

			long time = System.currentTimeMillis();
//			HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
//			Condition condition = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
//					.withAttributeValueList(new AttributeValue().withS(nodeId));
//			
//			scanFilter.put("nodeId", condition);
			ScanRequest scanRequest = new ScanRequest(tableName);
			ScanResult scanResult = dynamoDB.scan(scanRequest);
			
			if (dynamoLog) {
				System.out.println(" -> DynamoDB get all nodes...");
				System.out.println("..." + scanResult.toString());
			}
			numReads++;
			
			readTime += (System.currentTimeMillis() - time);
			
			if (scanResult.getCount() >= 1) {
				return scanResult.getItems();
			} else {
				return null;
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
		
		return null;
	}

	public Map<String, AttributeValue> newNode(String nodeId, String adj, String location) {
		
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		
		item.put("nodeId", new AttributeValue(nodeId));
		
		if (location != null) {
			item.put("location", new AttributeValue(location));
		}
		if (adj != null) {
			item.put("adj", new AttributeValue(adj));
		} else {
			item.put("adj", new AttributeValue("[]"));
		}
		
		return item;
	}

	public int getNumReads() {
		return numReads;
	}

	public int getNumAdds() {
		return numAdds;
	}

	public int getNumUpdates() {
		return numUpdates;
	}
	
	public long getReadTime() {
		return readTime;
	}

	public long getAddTime() {
		return addTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
