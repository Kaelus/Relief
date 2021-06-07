package relief.cloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import relief.cloud.ReliefDynamoDBDataManager.AWSRegionEnum;
import relief.util.DebugLog;
import relief.util.Timestamper;

public class ReliefDynamoDBHistoryManager implements ReliefDKVS {
	
	AmazonDynamoDB client;
	DynamoDB dynamoDB;
	String tableName = "ReliefDynamoDBHistoryManager";
    Table table;
    public static boolean consistentReads = false;
	public static enum AWSRegionEnum {LOCAL, LAN, SEOUL, LONDON, OHIO, UNKNOWN};
	public static AWSRegionEnum region = AWSRegionEnum.LOCAL;
	public static String backEndHistoryServerURL = "http://localhost:8000";
	
	public ReliefDynamoDBHistoryManager(String configName) throws IOException {
		DebugLog.log("ReliefDynamoDBHistoryManager constructor.");
		
		parseHistoryManagerConfig(configName);
		
		DebugLog.log("tableName given=" + tableName);
		DebugLog.log("region is=" + region);
		DebugLog.log("consistentRead is=" + consistentReads);

		if (region.equals(AWSRegionEnum.LOCAL)) {
			// To use the local version dynamodb for development
			client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
					new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
					.build();
		} else if (region.equals(AWSRegionEnum.LAN)) {
			client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
					new AwsClientBuilder.EndpointConfiguration(backEndHistoryServerURL, "us-east-1"))
					.build();
		} else if (region.equals(AWSRegionEnum.SEOUL)) {
			client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2).build();
		} else if (region.equals(AWSRegionEnum.LONDON)) {
			client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
		} else if (region.equals(AWSRegionEnum.OHIO)) {
			client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
		} else {
			// To use the actual AWS
			client = AmazonDynamoDBClientBuilder.defaultClient();
		}
		
		dynamoDB = new DynamoDB(client);

		// check if the given tableName already exists in the database
		ListTablesResult tableLists = client.listTables();
		if (tableLists.getTableNames().contains(tableName)) {
			System.out.println("The table name " + tableName + " already exists.");
			table = dynamoDB.getTable(tableName);
		} else {
			table = createTable(tableName);
		}
	}
	
	private static void parseHistoryManagerConfig(String configFile) {
		File confFile = new File(configFile);
		if (!confFile.exists()) {
			if (!confFile.mkdir()) {
				System.err.println("Unable to find " + confFile);
	            System.exit(1);
	        }
		}
		try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if (line.startsWith("historyManagerRegion")) {
		    	   String[] tokens = line.split("=");
		    	   String regionStr = tokens[1];
		    	   if (regionStr.equals("LOCAL")) {
		    		   region = AWSRegionEnum.LOCAL;
		    	   } else if (regionStr.equals("LAN")) {
		    		   region = AWSRegionEnum.LAN;
		    	   } else if (regionStr.equals("SEOUL")) {
		    		   region = AWSRegionEnum.SEOUL;
		    	   } else if (regionStr.equals("LONDON")) {
		    		   region = AWSRegionEnum.LONDON;
		    	   } else if (regionStr.equals("OHIO")) {
		    		   region = AWSRegionEnum.OHIO;
		    	   } else {
		    		   region = AWSRegionEnum.UNKNOWN;
		    	   }
		    	   DebugLog.log("historyKeyspaceRegion=" + region);
		    	   if (region.equals(AWSRegionEnum.UNKNOWN)) {
		    		   DebugLog.elog("Error: Cannot support Unknown dataKeyspaceType");
		    		   System.exit(1);
		    	   }
		       } else if (line.startsWith("historyKeyspaceName")) {
		    	   String[] tokens = line.split("=");
		    	   String tableName = tokens[1];
		    	   DebugLog.log("historyKeyspaceName=" + tableName);
		    	   if (tableName == null) {
		    		   DebugLog.elog("Error: historyKeyspaceName is null");
		    		   System.exit(1);
		    	   }
		       } else if (line.startsWith("historyManagerConsistentReads")) {
		    	   String[] tokens = line.split("=");
		    	   String consistentReadsStr = tokens[1];
		    	   DebugLog.log("consistentReadsStr=" + consistentReadsStr);
		    	   consistentReads = Boolean.parseBoolean(consistentReadsStr);
		       } else if (line.startsWith("backEndHistoryServerURL")) {
		    	   String[] tokens = line.split("=");
		    	   backEndHistoryServerURL = tokens[1];
		    	   DebugLog.log("backEndHistoryServerURL=" + backEndHistoryServerURL);
		       }
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Table createTable(String tableName) {
		Table retTable = null;
        try {
            DebugLog.log("Attempting to create table; please wait...");
            retTable = dynamoDB.createTable(tableName,
					Arrays.asList(new KeySchemaElement("keyspace", KeyType.HASH), // Partition
							                                                      // key
							new KeySchemaElement("timestamp", KeyType.RANGE)), // Sort key
					Arrays.asList(new AttributeDefinition("keyspace", ScalarAttributeType.S),
							new AttributeDefinition("timestamp", ScalarAttributeType.S)),
					new ProvisionedThroughput(10L, 10L));
    		retTable.waitForActive();
    		DebugLog.log("Success.  Table status: " + retTable.getDescription().getTableStatus());
        } catch (Exception e) {
        	DebugLog.elog("Unable to create table: ");
        	DebugLog.elog(e.getMessage());
        }
        return retTable;
	}
	
	public void deleteTable(String tableName) {
		Table tableToDelete = dynamoDB.getTable(tableName);
		tableToDelete.delete();
		try {
			tableToDelete.waitForDelete();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// close the key value store
	@Override
	public void finish() throws IOException {
		client.shutdown();
		dynamoDB.shutdown();
	}

	public ReliefDKVSResponse get(String key) throws IOException {
		ReliefDKVSResponse resp = new ReliefDKVSResponse();

		GetItemSpec spec = new GetItemSpec().withPrimaryKey(
				"keyspace", tableName, 
				"timestamp", key).withConsistentRead(consistentReads);
        try {
            System.out.println("Attempting to read the item...");
            Item outcome = table.getItem(spec);
            System.out.println("GetItem succeeded: " + outcome);
            Object outcomeValue = outcome.get("value");
    		resp.version = Timestamper.getTimestamp(); 
            resp.data = (byte[]) outcomeValue;
        }
        catch (Exception e) {
            System.err.println("Unable to read item: " + key);
            System.err.println(e.getMessage());
        }

        return resp;
	}

	public ReliefDKVSResponse get(String start, String end) throws IOException, ParseException {
		ReliefDKVSResponse resp = new ReliefDKVSResponse();

		SortedMap<String, byte[]> histSeg = null;
		Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#ks", "keyspace");
		nameMap.put("#ts", "timestamp");
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(":ksn", tableName);
        valueMap.put(":start", start);
        valueMap.put(":end", end);

        QuerySpec querySpec = new QuerySpec()
				.withKeyConditionExpression("#ks = :ksn and #ts between :start and :end")
				.withNameMap(nameMap)
				.withValueMap(valueMap);
		querySpec = querySpec.withConsistentRead(consistentReads);
		ItemCollection<QueryOutcome> items = null;
        Iterator<Item> iterator = null;
        Item item = null;

        DebugLog.log("Operations from " + start + " and " + end);
		items = table.query(querySpec);
		resp.version = Timestamper.getTimestamp(); 
		iterator = items.iterator();
		histSeg = new TreeMap<String, byte[]>();
		while (iterator.hasNext()) {
			item = iterator.next();
			histSeg.put(item.getString("timestamp"), item.getBinary("operation"));
		}
		resp.data = histSeg;

		return resp;
	}
	
	public ReliefDKVSResponse put(String key, byte[] value) throws IOException {
		ReliefDKVSResponse resp = new ReliefDKVSResponse();

		try {
            DebugLog.log("Adding a new item...");
            PutItemOutcome outcome = null;
   			DebugLog.log("Adding to historykeyspace");
   			outcome = table.putItem(new Item().withPrimaryKey("keyspace", tableName, 
   					"timestamp", key).withBinary("operation", value));
   			resp.version = Timestamper.getTimestamp(); 
            DebugLog.log("PutItem succeeded:\n" + outcome.getPutItemResult());
		} catch (Exception e) {
			DebugLog.elog("Unable to add item: " + key);
			DebugLog.elog(e.getMessage());
		}

		return resp;
	}

	public ReliefDKVSResponse remove(String key) {
		ReliefDKVSResponse resp = new ReliefDKVSResponse();
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey(new PrimaryKey("keyspace", tableName, 
        				"timestamp", key));
        try {
        	DebugLog.log("Attempting a delete...");
        	table.deleteItem(deleteItemSpec);
            DebugLog.log("DeleteItem succeeded");
        }
        catch (Exception e) {
        	DebugLog.elog("Unable to delete item: " + 
        			"keyspace=" + tableName + " " +
        			"timestamp=" + key);
        	DebugLog.elog(e.getMessage());
        }

		resp.version = Timestamper.getTimestamp(); 
		return resp;
	}
	
	@Override
	public void clear() {
		deleteTable(tableName);		
		createTable(tableName);
	}
	
}
