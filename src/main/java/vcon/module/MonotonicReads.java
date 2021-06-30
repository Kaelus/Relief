package vcon.module;

import java.util.ArrayList;

import vcon.ConsistencyModel;
import vcon.VCon;
import vcon.VConInput;

public class MonotonicReads {

	public ArrayList<String> getValidValues(VConInput input) {
		ArrayList<String> retList = null;
//		String lastValue = VCon.last_read.get(input.clientID);
//		//System.out.println("lastValue = " + lastValue);
//		boolean foundWrite = false;
//		for (VConInput in : VCon.history) {
//			//System.out.println("analyzing glob history for VConInput=" + in.toString());
//			if (in.opType.equals(ConsistencyModel.OpType.WR) && in.key.equals(input.key)) {
//				if ((lastValue == null) || in.val.equals(lastValue)) {
//					foundWrite = true;
//				}
//				if (foundWrite) {
//					if (retList == null) {
//						retList = new ArrayList<String>();
//					}
//					retList.add(in.val);
//				}
//			}
//		}
		return retList;
	}
}
