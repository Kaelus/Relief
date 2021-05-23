package relief.cloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.SortedMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import relief.communication.Message;
import relief.communication.MessageType;
import relief.communication.ReceiverXMLRPC;
import relief.communication.SenderXMLRPC;
import relief.util.DebugLog;
import relief.util.ObjectSerializer;
import relief.util.Timestamper;


public class ReliefController {
	
	
	/**
	 * These are immutable constants
	 */
	public enum DKVSType {DynamoDB, Unknown};
	
	// SCH means Strongly Consistent History
	// NoCRHU means we are not using CRHU, therefore there is no overhead for history logging
	// CRHU means we are using the mechanism that logs to the history using versions
	public enum HSModeType {SCH, NoCRHU, CRHU};
	
	/**
	 * Data Structure for Internal Usage
	 */
	class HistoryUpdate {
		public String version;
		public byte[] msgBytes;
	}
	
	/**
	 * These are shared variables
	 * 
	 */
	// storage related
	public static DKVSType dataManagerType;
	public static DKVSType historyManagerType;
	public static ReliefDKVS historyManager;
	public static ReliefDKVS dataManager;
	public static Thread historyUpdaterThread;
	public static BlockingQueue<HistoryUpdate> queue;

	// XMLRPC related
	private static ReceiverXMLRPC receiver;
	
	// logging info
	public static String reliefCtrlID;
	public static int reliefCtrlNo;
	
	// UI related
	public static ControlUserInterfaceRunner cui;
	public static Thread controlUIThread;

	// Address related
	public static String reliefAddress;
	public static String primaryAddress;
	
	// Evaluation related
	public static HSModeType histSrvMode;
	//public static AtomicInteger gs;
	public static SenderXMLRPC histUpForwarder;
	

	/**
	 * These are local Variables
	 */

	// logging info
	public String loggerID = null;
	
	
	/**
	 * This constructor is called multiple times. On client's request, WebServer
	 * creates a ReliefController instance on the process of creating a
	 * separate worker to handle the request. (out of our control)
	 * 
	 * Shared variable instantiation should be done in another constructor that
	 * is used for starting up the WebServer
	 * 
	 * NOTE: This should not be called by code that is not WebServer
	 * 
	 * @throws Exception
	 */
	public ReliefController() throws Exception {

		if (loggerID == null) {
			this.loggerID = reliefCtrlID + (reliefCtrlNo++);
		}

		DebugLog.log("ReliefController constructor each worker thread");
	}
	
	/**
	 * This constructor is to be used to start up the WebServer. This is meant
	 * to be called only once. Also, this should be the only constructor used by
	 * other than WebServer.
	 * 
	 * @param srvName
	 * @throws Exception
	 */
	public ReliefController(String srvName, String configFile) throws Exception {
		DebugLog.log("ReliefController constructor for main server thread");
		this.loggerID = srvName;
		reliefAddress = "127.0.0.1:10080";
		primaryAddress = "127.0.0.1:10080";
		histSrvMode = HSModeType.CRHU;
		
		parseConfigInit(configFile);

		switch (dataManagerType) {
		case DynamoDB:
			dataManager = new ReliefDynamoDBDataManager(configFile);
			break;
		default:
			break;
		}
		switch (historyManagerType) {
		case DynamoDB:
			historyManager = new ReliefDynamoDBHistoryManager(configFile);
			break;
		default:
			break;
		}
		queue = new LinkedBlockingDeque<HistoryUpdate>();
		HistoryUpdater histUpdater = new HistoryUpdater(queue);
		historyUpdaterThread = new Thread(histUpdater);
		historyUpdaterThread.start();
		reliefCtrlID = "reliefCtrl";
		reliefCtrlNo = 0;
		cui = new ControlUserInterfaceRunner(this);
		controlUIThread = new Thread(cui);
		controlUIThread.start();
		//gs = new AtomicInteger(0);
		if (reliefAddress.contentEquals(primaryAddress)) {
			DebugLog.log("This Relief node is Primary");
		} else {
			DebugLog.log("This Relief node is Secondary");
		}
		DebugLog.log("");
		String xmlrpcPrimaryAddress = "http://" + primaryAddress + "/xmlrpc";
		histUpForwarder = new SenderXMLRPC(xmlrpcPrimaryAddress, "ReliefCloudServer", "ReliefController.handleClientRequest");
	}

	private static void parseConfigInit(String configFile) {
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
		       if (line.startsWith("dkvsDataManagerType")) {
		    	   String[] tokens = line.split("=");
		    	   String dkvsTypeStr = tokens[1];
		    	   if (dkvsTypeStr.equals("DynamoDB")) {
		    		   dataManagerType = DKVSType.DynamoDB;
		    	   } else {
		    		   dataManagerType = DKVSType.Unknown;
		    	   }
		    	   DebugLog.log("dataManagerType=" + dataManagerType);
		    	   if (dataManagerType.equals(DKVSType.Unknown)) {
		    		   DebugLog.elog("Error: Cannot support Unknown dataKeyspaceType");
		    		   System.exit(1);
		    	   }
		       } else if (line.startsWith("dkvsHistoryManagerType")) {
		    	   String[] tokens = line.split("=");
		    	   String dkvsTypeStr = tokens[1];
		    	   if (dkvsTypeStr.equals("DynamoDB")) {
		    		   historyManagerType = DKVSType.DynamoDB;
		    	   } else {
		    		   historyManagerType = DKVSType.Unknown;
		    	   }
		    	   DebugLog.log("historyManagerType=" + historyManagerType);
		    	   if (historyManagerType.equals(DKVSType.Unknown)) {
		    		   DebugLog.elog("Error: Cannot support Unknown historyKeyspaceType");
		    		   System.exit(1);
		    	   }
		       } else if (line.startsWith("reliefAddress")) {
		    	   String[] tokens = line.split("=");
		    	   reliefAddress = tokens[1];
		    	   DebugLog.log("reliefAddress is configured as=" + reliefAddress);
		       } else if (line.startsWith("historyServerMode")) {
		    	   String[] tokens = line.split("=");
		    	   String histSrvModeStr = tokens[1];
		    	   if (histSrvModeStr.equals(HSModeType.SCH.toString())) {
		    		   histSrvMode = HSModeType.SCH;
		    	   } else if (histSrvModeStr.equals(HSModeType.NoCRHU.toString())) {
		    		   histSrvMode = HSModeType.NoCRHU;
		    	   } else if (histSrvModeStr.equals(HSModeType.CRHU.toString())) {
		    		   histSrvMode = HSModeType.CRHU;
		    	   } else {
		    		   DebugLog.elog("Unsupported history server mode given=" + histSrvModeStr);
		    		   System.exit(1);
		    	   }
		    	   DebugLog.log("history server mode is configured as=" + histSrvMode);
		       } else if (line.startsWith("primaryAddress")) {
		    	   String[] tokens = line.split("=");
		    	   primaryAddress = tokens[1];
		    	   DebugLog.log("primaryAddress is configured as=" + primaryAddress);
		       }
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * This function handles Client's request accordingly
	 */
	public Object handleClientRequest(byte[] clReq) throws Exception {

		Message srvmsg = null;
		Object returnObj = null;
		Message cmsg = (Message) ObjectSerializer.deserialize(clReq);
		
		DebugLog.log("ReliefController has received request for handleClientRequest",
				this.loggerID);
		
		switch (cmsg.type) {
		
		case MessageType.MSG_T_GET:
		case MessageType.MSG_T_READ_ATTEST:
			DebugLog.log("Get Request", this.loggerID);
			returnObj = handleGetRequest(cmsg);
			srvmsg = new Message();
			srvmsg.timestamp = Timestamper.getTimestamp();
			srvmsg.type = MessageType.MSG_T_ACK;
			srvmsg.senderID = reliefCtrlID;
			srvmsg.value = (byte[]) returnObj;
			break;
		case MessageType.MSG_T_PUT:
		case MessageType.MSG_T_WRITE_ATTEST:
			DebugLog.log("Put Request", this.loggerID);
			returnObj = handlePutRequest(cmsg);
			srvmsg = new Message();
			srvmsg.timestamp = Timestamper.getTimestamp();
			srvmsg.type = MessageType.MSG_T_ACK;
			srvmsg.senderID = reliefCtrlID;
			srvmsg.value = (byte[]) returnObj;
			break;
		case MessageType.MSG_T_READ_HIST:
			DebugLog.log("Read History Request", this.loggerID);
			returnObj = handleReadHistoryRequest(cmsg);
			srvmsg = new Message();
			srvmsg.timestamp = Timestamper.getTimestamp();
			srvmsg.type = MessageType.MSG_T_ACK;
			srvmsg.senderID = reliefCtrlID;
			srvmsg.value = (byte[]) returnObj;
			break;
		case MessageType.MSG_T_SCH_UP_FORWARD:
			DebugLog.log("Strongly Consistent History Update Forward");
			returnObj = handleStronglyConsistentHistoryWriteRequest(cmsg);
			srvmsg = new Message();
			srvmsg.timestamp = Timestamper.getTimestamp();
			srvmsg.type = MessageType.MSG_T_ACK;
			srvmsg.senderID = reliefCtrlID;
			srvmsg.value = (byte[]) returnObj;
			break;
		default:
			break;
		}

		return ObjectSerializer.serialize(srvmsg);
	}

	public Object handleGetRequest(Message cmsg) throws Exception {
		DebugLog.log("cmsg=" + cmsg.toString());
		ReliefDKVSResponse resp = dataManager.get(cmsg.key);
		byte[] retValue = (byte[]) resp.data;
		if (retValue != null) {
			DebugLog.log("read value successfully:" + new String(retValue, "UTF-8"));
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			cmsg.hash = digest.digest(retValue);
			// History Update
			if (histSrvMode.equals(HSModeType.SCH) && !reliefAddress.equals(primaryAddress)) {
				handleStronglyConsistentHistoryWriteRequest(cmsg);
			} else if (histSrvMode.equals(HSModeType.SCH) && reliefAddress.equals(primaryAddress)) {
				HistoryUpdate histUp = new HistoryUpdate();
				//int ver = gs.getAndIncrement();
				//histUp.version = "" + ver;
				histUp.version = Timestamper.getTimestamp();
				histUp.msgBytes = (byte[]) ObjectSerializer.serialize(cmsg).clone();
				queue.add(histUp);
			} else if (histSrvMode.equals(HSModeType.CRHU)) {
				String histKey = (String) resp.version;
				HistoryUpdate histUp = new HistoryUpdate();
				histUp.version = histKey;
				histUp.msgBytes = (byte[]) ObjectSerializer.serialize(cmsg).clone();
				queue.add(histUp);
			}
			// we don't do history update for NoCRHU mode
		}
		return retValue;
	}
	
	public Object handlePutRequest(Message cmsg) throws Exception {
		DebugLog.log("cmsg=" + cmsg.toString());
		ReliefDKVSResponse resp = dataManager.put(cmsg.key, cmsg.value);
		cmsg.value = null;
		if (histSrvMode.equals(HSModeType.SCH) && !reliefAddress.equals(primaryAddress)) {
			handleStronglyConsistentHistoryWriteRequest(cmsg);
		} else if (histSrvMode.equals(HSModeType.SCH) && reliefAddress.equals(primaryAddress)) {
			HistoryUpdate histUp = new HistoryUpdate();
			//int ver = gs.getAndIncrement();
			//histUp.version = "" + ver;
			histUp.version = Timestamper.getTimestamp();
			histUp.msgBytes = (byte[]) ObjectSerializer.serialize(cmsg).clone();
			queue.add(histUp);
		} else if (histSrvMode.equals(HSModeType.CRHU)) {
			String histKey = (String) resp.version;
			HistoryUpdate histUp = new HistoryUpdate();
			histUp.version = histKey;
			histUp.msgBytes = (byte[]) ObjectSerializer.serialize(cmsg).clone();
			queue.add(histUp);
		}
		
		// we don't do history update for NoCRHU mode
		return null;
	}
	
	public Object handleReadHistoryRequest(Message cmsg) throws Exception {
		DebugLog.log("cmsg=" + cmsg.toString());
		byte[] retValue = null;
		if (histSrvMode.equals(HSModeType.SCH) && !reliefAddress.equals(primaryAddress)) {
			// forward this cmsg to the primary node
			Message resp = histUpForwarder.send(cmsg);
			retValue = resp.value;
		} else if (histSrvMode.equals(HSModeType.CRHU)
				|| (histSrvMode.equals(HSModeType.SCH) && reliefAddress.equals(primaryAddress))) {
			// read the history by myself
			String rangeRequested = new String(cmsg.value, "UTF-8");
			String startTimeStr = rangeRequested.split("~")[0];
			String endTimeStr = rangeRequested.split("~")[1];
			ReliefDKVSResponse resp = historyManager.get(startTimeStr, endTimeStr);
			@SuppressWarnings("unchecked")
			SortedMap<String, byte[]> keyValues = (SortedMap<String, byte[]>) resp.data;
			retValue = ObjectSerializer.serialize(keyValues);
			//MessageDigest digest = MessageDigest.getInstance("SHA-256");
			//cmsg.hash = digest.digest(retValue);
			//String histKey = (String) resp.version;
			//HistoryUpdate histUp = new HistoryUpdate();
			//histUp.version = histKey;
			//histUp.msgBytes = (byte[]) ObjectSerializer.serialize(cmsg).clone();
			//queue.add(histUp);
		}
		return retValue;
	}
	
	public Object handleStronglyConsistentHistoryWriteRequest(Message cmsg) throws Exception {
		// Assert: This message type is allowed only under SCH mode.
		if (!histSrvMode.equals(HSModeType.SCH)) {
			DebugLog.elog("Assert: This message type is allowed only under SCH mode.");
			System.exit(1);
		}
		DebugLog.log("cmsg=" + cmsg.toString());
		byte[] retValue = null;
		if (reliefAddress.equals(primaryAddress)) {
			DebugLog.log("This is Primary. Update the history.");
			// update the history with my global sequence number
			HistoryUpdate histUp = new HistoryUpdate();
			//int ver = gs.getAndIncrement();
			//histUp.version = "" + ver;
			histUp.version = Timestamper.getTimestamp();
			if (DebugLog.VERBOSE) {
				Message msgToLog = (Message) ObjectSerializer.deserialize(cmsg.value);
				DebugLog.log("msgToLog=" + msgToLog.toString());
				histUp.msgBytes = (byte[]) ObjectSerializer.serialize(msgToLog).clone();
			} else {
				histUp.msgBytes = cmsg.value;
			}
			queue.add(histUp);
		} else {
			DebugLog.log("This is Secondary. Forward to the Primary.");
			// forward this cmsg to the primary node
			Message req = new Message();
			req.timestamp = Timestamper.getTimestamp();
			req.type = MessageType.MSG_T_SCH_UP_FORWARD;
			req.senderID = reliefAddress;
			req.value = ObjectSerializer.serialize(cmsg);
			Message resp = histUpForwarder.send(req);
			DebugLog.log(resp.toString());
		}
		return retValue;
	}
	
	public void runReliefController() {
		String portStr = reliefAddress.split(":")[1];
		//receiver = new ReceiverXMLRPC(8090);
		receiver = new ReceiverXMLRPC(Integer.parseInt(portStr));
		receiver.loggerID = this.loggerID;
	}

	public void stopReliefController() throws IOException {
		historyManager.finish();
		dataManager.finish();
		receiver.webServer.shutdown();
	}
		
	public void stopHistoryUpdater() {
		System.out.println("Interrupting the history updater thread to terminate");
		historyUpdaterThread.interrupt();
		try {
			historyUpdaterThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class HistoryUpdater implements Runnable {
		protected final BlockingQueue<HistoryUpdate> q;
		public HistoryUpdater(BlockingQueue<HistoryUpdate> q) { 
			this.q = q; 
		}
		@Override
		public void run() {
			DebugLog.log("[HistoryUpdater] run entered");
			try {
				while (true) { 
					HistoryUpdate histUp = q.take();
					synchronized(historyManager) {
						historyManager.put(histUp.version, histUp.msgBytes);
					}
				}
			} catch (IOException e) {
				DebugLog.elog(e.getMessage());
			} catch (InterruptedException e) { 
				DebugLog.log("[HistoryUpdater] Get interrupted");
			} 
			System.out.println("[HistoryUpdater] Terminating HistoryUpdater Run");
		}
	}
	

	public static void main(String args[]) throws Exception {
		String configFile = null;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-c") && i < args.length - 1) {
				System.out.println("configuration file is given=" + args[i+1]);
				configFile = args[i+1];
			}
		}
		if (configFile == null) {
			System.err.println("ERROR configFile is not given..");
			System.exit(1);
		}
		ReliefController relifCtrl = new ReliefController(
				"standalone-ReliefController-main", configFile);
		relifCtrl.runReliefController();
		DebugLog.log("Server started!");
	}

}
