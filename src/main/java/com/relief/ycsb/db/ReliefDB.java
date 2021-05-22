package com.relief.ycsb.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import com.yahoo.ycsb.ByteArrayByteIterator;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Utils;

import relief.client.ReliefClient;
import relief.util.DebugLog;
import relief.util.Timestamper;

public class ReliefDB extends DB {
	public static final String VERBOSE = "basicdb.verbose";
	public static final String VERBOSE_DEFAULT = "true";

	public static final String SIMULATE_DELAY = "basicdb.simulatedelay";
	public static final String SIMULATE_DELAY_DEFAULT = "0";

	public static final String CONSISTENCY_MODE = "basicdb.consistency";
	public static final String CONSISTENCY_MODE_DEFAULT = "EC";

	public static final String RESET_COUNT = "basicdb.resetcount";
	public static final String RESET_COUNT_DEFAULT = Integer.MAX_VALUE + "";

	public static final String CONFIG_FILE = "configFileName";
	public static final String CONFIG_FILE_DEFAULT = "data/reliefClient.conf";
	
	//public UnityClientEngine cl;
	public ReliefClient cl;
	public String configFileName;
	
	boolean verbose;
	int todelay;
	String consistencyMode;
	private long experimentStartTime;
	private long experimentDuration;

	int resetCount;
	
	public static int opCount;
	
	public ReliefDB() {
		todelay = 0;
		opCount = 0;
	}

	void delay() {
		if (todelay > 0) {
			try {
				long randToDelay = (long) Utils.random().nextInt(todelay);
				DebugLog.log("randomly picked todelay=" + randToDelay);
				Thread.sleep(randToDelay);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	void resetStats(int cnt) {
//		if (cnt > 0) {
//			if (cnt % resetCount == 0) {
//				try {
//					cl.flushStats();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
	/**
	 * Initialize any state for this DB. Called once per DB instance; there is
	 * one DB instance per client thread.
	 */
	@SuppressWarnings("unchecked")
	public void init() {
		verbose = Boolean.parseBoolean(getProperties().getProperty(VERBOSE,
				VERBOSE_DEFAULT));
		todelay = Integer.parseInt(getProperties().getProperty(SIMULATE_DELAY,
				SIMULATE_DELAY_DEFAULT));
		consistencyMode = getProperties().getProperty(CONSISTENCY_MODE,
				CONSISTENCY_MODE_DEFAULT);
		resetCount = Integer.parseInt(getProperties().getProperty(RESET_COUNT,
				RESET_COUNT_DEFAULT));
		configFileName = getProperties().getProperty(CONFIG_FILE, 
				CONFIG_FILE_DEFAULT);

		if (verbose) {
			System.out
					.println("***************** properties *****************");
			Properties p = getProperties();
			if (p != null) {
				for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
					String k = (String) e.nextElement();
					System.out.println("\"" + k + "\"=\"" + p.getProperty(k)
							+ "\"");
				}
			}
			System.out
					.println("**********************************************");
		} else {
			DebugLog.VERBOSE = false;
		}
		/*try {
			Thread.sleep(cl.me.replicationPeriod + 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
//			System.out.println("Do you want to join the client? [y/n]");
//			BufferedReader br = new BufferedReader(new InputStreamReader(
//					System.in));
//			String input = br.readLine();
//			if (input.equals("y")) {
//				cl.doClientJoin();
//			}
//			System.out.println("Doing initial heartbeat-key read to get all certificates");
//			Vector<String> key = new Vector<String>();
//			key.add("heartbeat-key");
//
//			if (consistencyMode.equals("EC")) {
//				cl.doGetEC(key);
//			} else if (consistencyMode.equals("CC")) {
//				cl.doGetCC(key.get(0));
//			} else if (consistencyMode.equals("SC")) {
//				cl.doGetSC(key.get(0));
//			} else {
//				System.err.println("None supported Consistency Mode="
//						+ consistencyMode);
//			}
//			System.out.println("initial heartbeat done.");
//			
//			//reset the stat before run the actual workload
//			cl.flushStats();
//			
//			System.out.println("Do you want to start client with Peacemaker enabled? [y/n]");
//			input = br.readLine();
//			if (input.equals("y")) {
//				cl.startPeacemaker();
//				System.out.println("timebound=" + cl.me.timebound);
//				System.out.println("hbfreq=" + cl.me.hbfreq);
//			}
			experimentStartTime = System.currentTimeMillis();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DebugLog.log("initializing ReliefDBClient.");
		try {
			DebugLog.log("Instantiate ReliefClient with configFileName=" + configFileName);
			cl = new ReliefClient(configFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}		

	}

	/**
	 * Read a record from the database. Each field/value pair from the result
	 * will be stored in a HashMap.
	 * 
	 * @param table
	 *            The name of the table
	 * @param key
	 *            The record key of the record to read.
	 * @param fields
	 *            The list of fields to read, or null for all of them
	 * @param result
	 *            A HashMap of field/value pairs for the result
	 * @return Zero on success, a non-zero error code on error
	 */
	public int read(String table, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		delay();

		int currOpCount = opCount++;
		resetStats(currOpCount);
		if (verbose) {
			System.out.print("["+ (currOpCount) + "] " + "READ " + table + " " + key + " [ ");
			if (fields != null) {
				for (String f : fields) {
					System.out.print(f + " ");
				}
			} else {
				System.out.print("<all fields>");
			}

			System.out.println("]");
		}

		//Ben's implementation below
		DebugLog.log("Unity Get about to be performed");
		DebugLog.log("["+ (currOpCount) + "] " + "READ " + table + " " + key);
		if (fields != null) {
			if (fields.size() > 1) {
				DebugLog.elog("Unity currently support only 1 field.");
				return -1;
			}
		}
		byte[] valueByteArray = null;
		try {
//			if (consistencyMode.equals("EC")) {
//				valueByteArray = cl.getEC(key, GetFlagType.GET_FLAG_T_CLOUD);
//			} else if (consistencyMode.equals("CC")) {
//				valueByteArray = cl.doGetCC(key);
//			} else if (consistencyMode.equals("SC")){
//				valueByteArray = cl.doGetSC(key);
//			} else {
//				System.err.println("None supported Consistency Mode=" + consistencyMode);
//			}
			valueByteArray = cl.get(key, "l");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		if (valueByteArray == null) {
			DebugLog.log("getEC failed");
			return -1;
		}
		String retStr = new String(valueByteArray);
		ByteArrayByteIterator babi = new ByteArrayByteIterator(valueByteArray); 
		if (fields == null) {
			result.put("nullfield", babi);
		} else {
			for (String field : fields) {
				result.put(field, babi);
			}
		}
		DebugLog.log("read return=" + retStr);
		DebugLog.log("Unity Get completed");
		
		return 0;
	}

	/**
	 * Perform a range scan for a set of records in the database. Each
	 * field/value pair from the result will be stored in a HashMap.
	 * 
	 * @param table
	 *            The name of the table
	 * @param startkey
	 *            The record key of the first record to read.
	 * @param recordcount
	 *            The number of records to read
	 * @param fields
	 *            The list of fields to read, or null for all of them
	 * @param result
	 *            A Vector of HashMaps, where each HashMap is a set field/value
	 *            pairs for one record
	 * @return Zero on success, a non-zero error code on error
	 */
	public int scan(String table, String startkey, int recordcount,
			Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
		delay();

		int currOpCount = opCount++;
		resetStats(currOpCount);
		if (verbose) {
			System.out.print("["+ (currOpCount) + "] " + "SCAN " + table + " " + startkey + " "
					+ recordcount + " [ ");
			if (fields != null) {
				for (String f : fields) {
					System.out.print(f + " ");
				}
			} else {
				System.out.print("<all fields>");
			}

			System.out.println("]");
		}

		//Ben's implementation below
		//Unity don't support scan yet. CoreWorkload's default scan proportion is also 0. Skip it for now.
		//DebugLog.elog("Unity does not support Scan semantics");
		//return -1;
		DebugLog.elog("we do history reads instead");
		DebugLog.log("["+ (currOpCount) + "] " + "History READ " + table);
		String startTime = null;
		try {
			startTime = Timestamper.incTimestamp(cl.lastHistTimestamp, 1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
		String endTime = sdf.format(new Date(Long.MAX_VALUE));
		DebugLog.log("cl.lastHistTimestamp=" + cl.lastHistTimestamp);
		DebugLog.log("Reading a history segment between " + startTime + " and " + endTime);
		try {
			cl.readHistory(startTime, endTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
		
		//return 0;
	}

	/**
	 * Update a record in the database. Any field/value pairs in the specified
	 * values HashMap will be written into the record with the specified record
	 * key, overwriting any existing values with the same field name.
	 * 
	 * @param table
	 *            The name of the table
	 * @param key
	 *            The record key of the record to write.
	 * @param values
	 *            A HashMap of field/value pairs to update in the record
	 * @return Zero on success, a non-zero error code on error
	 */
	public int update(String table, String key,
			HashMap<String, ByteIterator> values) {
		delay();

		int currOpCount = opCount++;
		resetStats(currOpCount);
		if (verbose) {
			System.out.print("["+ (currOpCount) + "] " + "UPDATE " + table + " " + key + " [ ");
			if (values != null) {
				for (String k : values.keySet()) {
					//BE CAREFULL values.get(k) is mutating the ByteIterator instance.
					//System.out.print(k + "=" + values.get(k) + " ");
				}
			}
			System.out.println("]");
		}

		// Ben's implementation below
		DebugLog.log("[update] Unity Put about to be performed");
		if (values.size() > 1) {
			DebugLog.elog("[update] Unity currently support only 1 field.");
			return -1;
		}
		byte[] valueByteArray = null;
		/*for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
			valueByteArray = entry.getValue().toArray();
		}*/
		for (String k : values.keySet()) {
			valueByteArray = values.get(k).toArray();
		}
		DebugLog.log("valueByteArray to String=" + new String(valueByteArray));
		try {
			/*byte[] counterString = StringBytesConverter.stringToBytes("[" + opCount + "]");
			byte[] concatValueByteArray = new byte[valueByteArray.length + counterString.length];
			System.arraycopy(counterString, 0, concatValueByteArray, 0, counterString.length);
			System.arraycopy(valueByteArray, 0, concatValueByteArray, counterString.length, valueByteArray.length);
			cl.putEC(key, concatValueByteArray, GetFlagType.GET_FLAG_T_CLOUD);*/
			
			//ex: YCSB: 10 clientA TIME= 123432345 !@b#@23wwf4#$3a1^%$#%!@wrd1443#@!4...
			//String counterStr = "YCSB: " + opCount + " " + cl.me.cid + " " + "TIME= " + System.currentTimeMillis() + " ";
			//RandomString rs = new RandomString(valueByteArray.length - counterStr.length());
			//String counterStrRandomData = counterStr + rs.nextString();
			//byte[] valueBytes = StringBytesConverter.stringToBytes(counterStrRandomData);
			byte[] valueBytes = valueByteArray;
			
//			if (consistencyMode.equals("EC")) {
//				cl.putEC(key, valueBytes, GetFlagType.GET_FLAG_T_CLOUD);
//			} else if (consistencyMode.equals("CC")) {
//				cl.doPutCC(key, valueBytes);
//			} else if (consistencyMode.equals("SC")){
//				cl.doPutSC(key, valueBytes);
//			} else {
//				System.err.println("None supported Consistency Mode=" + consistencyMode);
//			}
			cl.put(key, valueBytes);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		DebugLog.log("[update] Unity Put completed");

		return 0;
	}

	/**
	 * Insert a record in the database. Any field/value pairs in the specified
	 * values HashMap will be written into the record with the specified record
	 * key.
	 * 
	 * @param table
	 *            The name of the table
	 * @param key
	 *            The record key of the record to insert.
	 * @param values
	 *            A HashMap of field/value pairs to insert in the record
	 * @return Zero on success, a non-zero error code on error
	 */
	public int insert(String table, String key,
			HashMap<String, ByteIterator> values) {
		delay();
		
		int currOpCount = opCount++;
		resetStats(currOpCount);
		if (verbose) {
			System.out.print("["+ (currOpCount) + "] " + "INSERT " + table + " " + key + " [ ");
			if (values != null) {
				for (String k : values.keySet()) {
					//BE CAREFULL values.get(k) is mutating the ByteIterator instance.
					//System.out.print(k + "=" + values.get(k) + " ");
				}
			}

			System.out.println("]");
		}
		
		// Ben's implementation below
		DebugLog.log("[insert] Unity put is about to be called");
		if (values.size() > 1) {
			DebugLog.elog("[insert] Unity currently support only 1 field.");
			return -1;
		}
		byte[] valueByteArray = null;
		/*for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
			valueByteArray = entry.getValue().toArray();
		}*/
		for (String k : values.keySet()) {
			valueByteArray = values.get(k).toArray();
		}
		DebugLog.log("valueByteArray to String=" + new String(valueByteArray));
		try {
			
//			String counterStr = "YCSB: " + opCount + " " + cl.me.cid + " " + "TIME= " + System.currentTimeMillis() + " ";
//			RandomString rs = new RandomString(valueByteArray.length - counterStr.length());
//			String counterStrRandomData = counterStr + rs.nextString();
//			byte[] valueBytes = StringBytesConverter.stringToBytes(counterStrRandomData);
			byte[] valueBytes = valueByteArray;
			
//			if (consistencyMode.equals("EC")) {
//				cl.putEC(key, valueBytes, GetFlagType.GET_FLAG_T_CLOUD);
//			} else if (consistencyMode.equals("CC")) {
//				cl.doPutCC(key, valueBytes);
//			} else if (consistencyMode.equals("SC")){
//				cl.doPutSC(key, valueBytes);
//			} else {
//				System.err.println("None supported Consistency Mode=" + consistencyMode);
//			}

			cl.put(key, valueBytes);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		DebugLog.log("[insert] Unity put completed");
		
		return 0;
	}

	/**
	 * Delete a record from the database.
	 * 
	 * @param table
	 *            The name of the table
	 * @param key
	 *            The record key of the record to delete.
	 * @return Zero on success, a non-zero error code on error
	 */
	public int delete(String table, String key) {
		delay();

		int currOpCount = opCount++;
		resetStats(currOpCount);
		if (verbose) {
			System.out.println("["+ (currOpCount) + "] " + "DELETE " + table + " " + key);
		}
		
		// Ben's implementation below
		DebugLog.elog("Unity does not support Delete semantics");
		return -1;
		
		//return 0;
	}

	/**
	 * Short test of BasicDB
	 */
	/*
	 * public static void main(String[] args) { BasicDB bdb=new BasicDB();
	 * 
	 * Properties p=new Properties(); p.setProperty("Sky","Blue");
	 * p.setProperty("Ocean","Wet");
	 * 
	 * bdb.setProperties(p);
	 * 
	 * bdb.init();
	 * 
	 * HashMap<String,String> fields=new HashMap<String,String>();
	 * fields.put("A","X"); fields.put("B","Y");
	 * 
	 * bdb.read("table","key",null,null); bdb.insert("table","key",fields);
	 * 
	 * fields=new HashMap<String,String>(); fields.put("C","Z");
	 * 
	 * bdb.update("table","key",fields);
	 * 
	 * bdb.delete("table","key"); }
	 */

	
	public void cleanup() throws DBException
	{
		
		DebugLog.log("cleanup called");
		experimentDuration = System.currentTimeMillis() - experimentStartTime;
		System.out.println("Experiments ran for=" + experimentDuration);

//		boolean finishFlag= false;
//		while(!finishFlag) {
//			
//			System.out.println("size of unverified put operation list=" + cl.unverifiedPutList.size());
//			System.out.println("size of unverified get operation list=" + cl.unverifiedGetList.size());
//		
//			int i=0;
//			for(UnverifiedPut unvput : cl.unverifiedPutList) {
//				System.out.println((i++)+"-th unverified put=\n" + unvput.toString());
//			}
//			i=0;
//			for(UnverifiedGet unvget : cl.unverifiedGetList) {
//				System.out.println((i++)+"-th unverified get=\n" + unvget.toString());
//			}
//			
//			experimentDuration = System.currentTimeMillis() - experimentStartTime;
//			System.out.println("Experiments ran for=" + experimentDuration);
//			
//			System.out.println("Do you want to finish the client? [y/n]");
//			BufferedReader br = new BufferedReader(new InputStreamReader(
//					System.in));
//			String input = null;
//			try {
//				input = br.readLine();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			if (input.equals("y")) {
//				finishFlag = true;
//			}
//			Vector<String> key = new Vector<String>();
//			key.add("heartbeat-key");
//			try {
//				if (consistencyMode.equals("EC")) {
//					cl.doGetEC(key);
//				} else if (consistencyMode.equals("CC")) {
//					cl.doGetCC(key.get(0));
//				} else if (consistencyMode.equals("SC")){
//					cl.doGetSC(key.get(0));
//				} else {
//					System.err.println("None supported Consistency Mode=" + consistencyMode);
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		//DebugLog.log("[cleanup] statistic: " + UnityClientMeasurements.getOutputString() + "\n");
	}

}
