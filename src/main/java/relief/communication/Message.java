package relief.communication;

import java.io.UnsupportedEncodingException;

public class Message  implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Fields contained in a Message
	public String timestamp;
	public int type;
	public String senderID;
	public long count;
	public String key;
	public byte[] value;
	public byte[] hash; // MAC
	public byte[] signature; // Digital Signature
	
	public Message() {
		//initialization
		timestamp = null;
		type = MessageType.MSG_T_UNDEFINED;
		senderID = null;
		count = -1;
		key = null;
		hash = null;
		value = null;
		signature = null;
	}
	
	public String toString() {
		String messageString = "";
		messageString += "=== Message Description BEGIN ===\n";
		messageString += "timestamp: " + timestamp + "\n";
		String typeStr = null;
		switch (type) {
		case MessageType.MSG_T_UNDEFINED:
			typeStr = "MSG_T_UNDEFINED";
			break;
		case MessageType.MSG_T_ACK:
			typeStr = "MSG_T_ACK";
			break;
		case MessageType.MSG_T_GET:
			typeStr = "MSG_T_GET";
			break;
		case MessageType.MSG_T_PUT:
			typeStr = "MSG_T_PUT";
			break;
		case MessageType.MSG_T_READ_HIST:
			typeStr = "MSG_T_READ_HIST";
			break;
		case MessageType.MSG_T_WRITE_ATTEST:
			typeStr = "MSG_T_ATTEST";
			break;
		default:
			break;
		}
		messageString += "type: " + typeStr + " (" + type + ")" + "\n";
		messageString += "senderID: " + senderID + "\n";
		messageString += "count: " + count + "\n";
		messageString += "key: " + key + "\n";
		try {
			if (value == null) {
				messageString += "value: " + value + "\n";
			} else {
				messageString += "value: " + new String(value, "UTF-8") + "\n";
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		messageString += "hash: " + hash + "\n";
		messageString += "signature: " + signature + "\n";
		messageString += "=== Message Description END ===\n\n";
		
		return messageString;
	}
	
}

