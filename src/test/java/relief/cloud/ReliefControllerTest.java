package relief.cloud;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import relief.communication.Message;
import relief.communication.MessageType;
import relief.util.DebugLog;
import relief.util.ObjectSerializer;
import relief.util.Timestamper;

public class ReliefControllerTest {

	@Test
	public void testHistoryUpdateCRHU() throws Exception {
		ReliefController ctrl = new ReliefController("ReliefControllerTest", "conf/relief.conf");
		ctrl.histSrvMode = ReliefController.HSModeType.CRHU;
		ctrl.reliefAddress = "127.0.0.1:10080";
		ctrl.primaryAddress = "127.0.0.1:10080";
		ArrayList<Message> msgList = new ArrayList<Message>();
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		Message msg = new Message();
		msg.type = MessageType.MSG_T_PUT;
		msg.key = "Hello";
		msg.value = "World".getBytes();
		msg.hash = digest.digest(msg.value);
		String startTS = Timestamper.getTimestamp();
		ctrl.handlePutRequest(msg);
		msg.value = "World".getBytes(); // since handlePutRequest set it null
		msgList.add(msg);
		Message msg2 = new Message();
		msg2.type = MessageType.MSG_T_GET;
		msg2.key = "Hello";
		byte[] retVal = (byte[]) ctrl.handleGetRequest(msg2);
		Assert.assertArrayEquals("World".getBytes(), retVal);
		msgList.add(msg2);
		String endTS = Timestamper.getTimestamp();
		Message msg3 = new Message();
		String rangeRequest = startTS + "~" + endTS;
		DebugLog.log("Sending the following as value = " + rangeRequest);
		msg3.value = rangeRequest.getBytes();
		SortedMap<String, byte[]> histSeg = (SortedMap<String, byte[]>) ObjectSerializer.deserialize((byte[]) ctrl.handleReadHistoryRequest(msg3));
		Iterator<Entry<String, byte[]>> iter = histSeg.entrySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			Entry<String, byte[]> entry = (Entry<String, byte[]>) iter.next();
			String timestamp = entry.getKey();
			String valStr = ((Message) ObjectSerializer.deserialize(entry.getValue())).toString();
			DebugLog.log(timestamp + "->" + valStr);
			Assert.assertEquals(msgList.get(i).key, ((Message) ObjectSerializer.deserialize(entry.getValue())).key);
			Assert.assertArrayEquals(msgList.get(i).hash, (((Message) ObjectSerializer.deserialize(entry.getValue())).hash));
		}
	}

	@Test
	public void testHistoryUpdateSCHPrimary() throws Exception {
		ReliefController ctrl = new ReliefController("ReliefControllerTest", "conf/relief.conf");
		ctrl.histSrvMode = ReliefController.HSModeType.SCH;
		ctrl.reliefAddress = "127.0.0.1:10080";
		ctrl.primaryAddress = "127.0.0.1:10080";
		ArrayList<Message> msgList = new ArrayList<Message>();
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		Message msg = new Message();
		msg.type = MessageType.MSG_T_PUT;
		msg.key = "Hello";
		msg.value = "World".getBytes();
		msg.hash = digest.digest(msg.value);
		String startTS = Timestamper.getTimestamp();
		ctrl.handlePutRequest(msg);
		msg.value = "World".getBytes(); // since handlePutRequest set it null
		msgList.add(msg);
		Message msg2 = new Message();
		msg2.type = MessageType.MSG_T_GET;
		msg2.key = "Hello";
		byte[] retVal = (byte[]) ctrl.handleGetRequest(msg2);
		Assert.assertArrayEquals("World".getBytes(), retVal);
		msgList.add(msg2);
		String endTS = Timestamper.getTimestamp();
		Message msg3 = new Message();
		String rangeRequest = startTS + "~" + endTS;
		DebugLog.log("Sending the following as value = " + rangeRequest);
		msg3.value = rangeRequest.getBytes();
		SortedMap<String, byte[]> histSeg = (SortedMap<String, byte[]>) ObjectSerializer.deserialize((byte[]) ctrl.handleReadHistoryRequest(msg3));
		Iterator<Entry<String, byte[]>> iter = histSeg.entrySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			Entry<String, byte[]> entry = (Entry<String, byte[]>) iter.next();
			String timestamp = entry.getKey();
			String valStr = ((Message) ObjectSerializer.deserialize(entry.getValue())).toString();
			DebugLog.log(timestamp + "->" + valStr);
			Assert.assertEquals(msgList.get(i).key, ((Message) ObjectSerializer.deserialize(entry.getValue())).key);
			Assert.assertArrayEquals(msgList.get(i).hash, (((Message) ObjectSerializer.deserialize(entry.getValue())).hash));
		}
	}
}
