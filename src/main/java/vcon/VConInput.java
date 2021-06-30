package vcon;

import java.io.Serializable;

public class VConInput implements Serializable {

	/* input format
	 * <Input> = <ClientID>.<Key>.<OpType>.<Value>.<Timestamp>
	 */
	public String clientID;
	public String key;
	public ConsistencyModel.OpType opType;
	public String val;
	public long timestamp;
	
	// To keep track how many VConInput has been created so far
	public static long reqCnt;
	public long privReqCnt;
	
	static {
		reqCnt = 0;
	}

	public VConInput(String clientID, String key, ConsistencyModel.OpType opType,
			String val, long timestamp) {
		this.clientID = clientID;
		this.key = key;
		this.opType = opType;
		this.val = val;
		this.timestamp = timestamp;
		privReqCnt = ++reqCnt;
	}
	
	public String toString() {
		String retStr = "";
		retStr += "privReqCnt=" + privReqCnt + " ";
		retStr += "clientID=" + clientID + " ";
		retStr += "key=" + key + " ";
		retStr += "opType=" + opType + " ";
		/*if (opType.equals(ConsistencyModel.OpType.RD)) {
			retStr += "(RD)" + " ";
		} else if (opType.equals(ConsistencyModel.OpType.WR)) {
			retStr += "(WR)" + " ";
		} else {
			retStr += "(UNKNOWN)" + " ";
		}*/
		retStr += "val=" + val + " ";
		retStr += "timestamp=" + timestamp + "\n";
		return retStr;
	}
	
}
