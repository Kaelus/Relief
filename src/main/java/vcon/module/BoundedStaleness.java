package vcon.module;

import java.util.ArrayList;

import vcon.ConsistencyModel;
import vcon.VCon;
import vcon.VConInput;

public class BoundedStaleness {

	public ArrayList<String> getValidValues(VConInput input) {
		ArrayList<String> retList = null;
//		long oldestTimestamp = input.timestamp - VCon.timebound;
//		for (VConInput in : VCon.history) {
//			if (in.opType.equals(ConsistencyModel.OpType.WR)) {
//				if (in.timestamp >= oldestTimestamp) {
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
