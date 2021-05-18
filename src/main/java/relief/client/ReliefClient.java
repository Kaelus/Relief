package relief.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.UUID;

import relief.communication.Message;
import relief.communication.MessageType;
import relief.communication.SenderXMLRPC;
import relief.util.DebugLog;
import relief.util.DigSig;
import relief.util.ObjectSerializer;
import relief.util.Timestamper;

public class ReliefClient {

	public String srvip;

	// This Client
	public ReliefClientStatus me;

	public DigSig digSig;
	
	// Nested class for clients
	public class ReliefClientStatus {

		//client ID
		public String cid;
		// local seq no
		public long ls;
		// latest global seq no the client saw so far
		public long lgs;
		// HB freq in sec
		public long hbfreq;
		// role -- regular client or watcher
		public int role;
		//Maximum watcher's update's global sequence number the client saw so far
		public long wgs;
		//timestamp for the last watcher's update
		public String lastWatcherTS; 
		//confirmed global sequence no by watcher 
		public long cgs;
		//60 sec is default timeout for watcher udpate, but is configurable
		public int WTO = 60000; 
		
		//followings are T, R and D respectively
		public long watcherPeriod;
		public long replicationPeriod;
		public long fudgeFactor;
		
		public ReliefClientStatus () {
			
		}
		
	}
	
	// sender
	private SenderXMLRPC sender;

	// logging info
	public String loggerID = null;
	
	// command constants
	//private static final int CMD_CSINTER_TEST = 3;

	// timestamp of the history segment lastly seen
	public String lastHistTimestamp;
	
	// constructor
	public ReliefClient() throws Exception {
		
		me = new ReliefClientStatus();
		me.cid = getCID();
		me.ls = 0;

		sender = new SenderXMLRPC();
		sender.loggerID = this.loggerID;
		srvip = "127.0.0.1"; // Hard coded
		//doRegister();

		lastHistTimestamp = Timestamper.convertToString(new Date(0L));
	}
	
	public ReliefClient(String configFile) throws Exception {

		parseConfigInit(configFile);
		
		me = new ReliefClientStatus();
		me.cid = getCID();
		me.ls = 0;

		String srvURL = "http://" + srvip + "/xmlrpc";
		sender = new SenderXMLRPC(srvURL, "ReliefController","ReliefController.handleClientRequest");
		sender.loggerID = this.loggerID;
		
		lastHistTimestamp = Timestamper.convertToString(new Date(0L));

		digSig = new DigSig(configFile);
		
	}

	private void parseConfigInit(String configFile) {
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
		       if (line.startsWith("srvip")) {
		    	   String[] tokens = line.split("=");
		    	   srvip = tokens[1];
		    	   DebugLog.log("srvip=" + srvip);
		       }
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getCID() throws Exception {
		/*
		 * long threadId = Thread.currentThread().getId(); return threadId + "@"
		 * + InetAddress.getLocalHost().getHostAddress();
		 */
		//return UUID.randomUUID() + "@"
		//		+ InetAddress.getLocalHost().getHostAddress();
		return UUID.randomUUID().hashCode() + "";
	}

	public byte[] get(String key, String flag) throws Exception {
		Message req = new Message();
		req.timestamp = Timestamper.getTimestamp();
		req.type = MessageType.MSG_T_GET;
		req.senderID = me.cid;
		req.count = me.ls++;
		req.key = key;
		Message resp = this.sender.send(req);
		DebugLog.log(resp.toString());
		return resp.value;
	}

	public void put(String key, byte[] value) throws Exception {
		Message req = new Message();
		req.timestamp = Timestamper.getTimestamp();
		req.type = MessageType.MSG_T_PUT;
		req.senderID = me.cid;
		req.count = me.ls++;
		req.key = key;
		req.value = value;
		Message resp = sender.send(req);
		if (resp.type == MessageType.MSG_T_ACK) {
			DebugLog.log("server responded with ACK");
		} else {
			DebugLog.elog("server NACK. For now, we just exit.");
			System.exit(1);
		}
	}

	@SuppressWarnings("unchecked")
	public SortedMap<String, byte[]> readHistory(String start, String end) throws Exception {
		Message req = new Message();
		req.timestamp = Timestamper.getTimestamp();
		req.type = MessageType.MSG_T_READ_HIST;
		req.senderID = me.cid;
		req.count = me.ls++;
		String rangeRequest = start + "~" + end;
		DebugLog.log("Sending the following as value = " + rangeRequest);
		req.value = rangeRequest.getBytes(); 
		Message resp = sender.send(req);
		SortedMap<String, byte[]> histSeg = (SortedMap<String, byte[]>) ObjectSerializer.deserialize(resp.value);
		if (!histSeg.isEmpty()) {
			lastHistTimestamp = histSeg.lastKey();
		} else {
			DebugLog.log("Returned history segment is empty");
		}
		return histSeg;
	}
	
	private void writeAttest(SortedMap<String, byte[]> histSeg) throws Exception {
		byte[] histSegHash = ObjectSerializer.serialize(histSeg);
		byte[] attestation = digSig.getDigSig(histSegHash);
		Message req = new Message();
		req.timestamp = Timestamper.getTimestamp();
		req.type = MessageType.MSG_T_WRITE_ATTEST;
		req.senderID = me.cid;
		req.count = me.ls++;
		if (!histSeg.isEmpty()) {
			req.key = "Attestation-" + histSeg.firstKey() + "~" + histSeg.lastKey();
			req.value = attestation;
		} else {
			req.key = "Attestation-" + "Empty";
			req.value = null;
		}
		Message resp = sender.send(req);
		if (resp.type == MessageType.MSG_T_ACK) {
			DebugLog.log("server responded with ACK");
		} else {
			DebugLog.elog("server NACK. For now, we just exit.");
			System.exit(1);
		}
	}
	
	/*
	 * private void doRegister() throws Exception { Message regMsg = new Message();
	 * regMsg.clientID = me.cid; regMsg.type = MessageType.MSG_T_REGISTER_CLIENT;
	 * Message srvmsg = sender.send(regMsg); if (srvmsg.type ==
	 * MessageType.MSG_T_ACK) { DebugLog.log("server responds with ACK.",
	 * this.loggerID);
	 * 
	 * } else { // Server NACK... // For now, we just exit.
	 * DebugLog.elog("SEVERE: Server NACK. For now, we just exit.", this.loggerID);
	 * System.exit(1); } }
	 */

	/*
	 * This function is basically for testing purpose for client to server
	 * interaction functionality of the implementation.
	 * 
	 * The function takes an input from the user determining what to do with the
	 * Unity. The user can read or write to the storage. Also, the user can
	 * trigger sending Update to the server (originally, it should be triggered
	 * by the timer.)
	 */
	private static void sendClientRequests(ReliefClient cl) throws Exception {
		// TODO Auto-generated method stub

		boolean quitFlag = false;

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String input;
			DebugLog.log("<sendClientRequests> What do you want to do? [1] quit [2] get [3] put [4] read history [5] attestation");
			while (!quitFlag && ((input = br.readLine()) != null)) {
				try {
					int cmd = Integer.valueOf(input);
					switch (cmd) {
					case 1:
						DebugLog.log("[cmd=" + cmd
								+ "] quit:");
						quitFlag = true;
						break;
					case 2:
						DebugLog.log("[cmd=" + cmd
								+ "] Reading... Enter Key to get:");
						input = br.readLine();
						// reading from the storage
						byte[] value = cl.get(input, "l");
						if (value == null) {
							DebugLog.log("Such Key does not exist.");
						} else {
							DebugLog.log("value is " + new String(value));
						}
						DebugLog.log("Get is Done...");

						break;
					case 3:
						DebugLog.log("[cmd=" + cmd
								+ "] Writing... Enter Key to put:");
						input = br.readLine();
						String key = input;
						DebugLog.log("Now enter value to put:");
						input = br.readLine();
						value = input.getBytes();
						try {
							// writing to the storage
							cl.put(key, value);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							DebugLog.log("ill formed arguments");
							e1.printStackTrace();
						}
						DebugLog.log("Writing Done...");

						break;
					case 4:
						DebugLog.log("[cmd=" + cmd
								+ "] Reading History:");
						String startTime = Timestamper.incTimestamp(cl.lastHistTimestamp, 1);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
						String endTime = sdf.format(new Date(Long.MAX_VALUE));
						DebugLog.log("cl.lastHistTimestamp=" + cl.lastHistTimestamp);
						DebugLog.log("Reading a history segment between " + startTime + " and " + endTime);
						SortedMap<String, byte[]> histSeg = cl.readHistory(startTime, endTime);
						Iterator<Entry<String, byte[]>> iter = histSeg.entrySet().iterator();
						while (iter.hasNext()) {
							Entry<String, byte[]> entry = (Entry<String, byte[]>) iter.next();
							String timestamp = entry.getKey();
							String valStr = ((Message) ObjectSerializer.deserialize(entry.getValue())).toString();
							DebugLog.log(timestamp + "->" + valStr);
						}
						DebugLog.log("Done with reading a history seg");
						break;
					case 5:
						DebugLog.log("[cmd=" + cmd
								+ "] Attestation:");
						startTime = Timestamper.incTimestamp(cl.lastHistTimestamp, 1);
						sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
						endTime = sdf.format(new Date(Long.MAX_VALUE));
						DebugLog.log("cl.lastHistTimestamp=" + cl.lastHistTimestamp);
						DebugLog.log("Reading a history segment between " + startTime + " and " + endTime);
						histSeg = cl.readHistory(startTime, endTime);
						iter = histSeg.entrySet().iterator();
						while (iter.hasNext()) {
							Entry<String, byte[]> entry = (Entry<String, byte[]>) iter.next();
							String timestamp = entry.getKey();
							String valStr = ((Message) ObjectSerializer.deserialize(entry.getValue())).toString();
							DebugLog.log(timestamp + "->" + valStr);
						}
						DebugLog.log("Done with reading a history seg");
						DebugLog.log("Writing Attestation..");
						cl.writeAttest(histSeg);
						break;
					default:
						break;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				DebugLog.log("<sendClientRequests> What do you want to do? [1] quit [2] get [3] put [4] read history [5] attestation");				
			}

		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	

	/**
	 * 
	 * main function for the testing and debugging purpose.
	 * 
	 * @param args
	 * @throws Exception
	 */

	public static void main(String[] args) throws Exception {

		boolean quitFlag = false;
		
		String loggerID = "standalone-ReliefClient-main";

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
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String input;
			System.out.println("<main> What do you want to do? [1] quit "
							+ "[2] sends client requests to the server ");
			ReliefClient cl = null;
			if (configFile == null) {
				cl = new ReliefClient();
			} else {
				cl = new ReliefClient(configFile);
			}
			
			cl.loggerID = loggerID;
			while (!quitFlag && ((input = br.readLine()) != null)) {
				try {
					int cmd = Integer.valueOf(input);
					switch (cmd) {
					case 1:
						quitFlag = true;
						break;
					case 2:
						DebugLog.log("[cmd=" + cmd
								+ "] Sending Client Requests to Servers");
						// client-server interaction test
						ReliefClient.sendClientRequests(cl);
						break;
					default:
						break;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (!quitFlag) {
					System.out.println("<main> What do you want to do? [1] quit "
							+ "[2] sends client requests to the server ");
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			DebugLog.elog("JavaClient: " + exception, loggerID);
		}

		DebugLog.log("The main function is Done now...", loggerID);
		DebugLog.log("Goodbye!!!", loggerID);

	}

}
