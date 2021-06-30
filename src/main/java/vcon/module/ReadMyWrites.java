package vcon.module;

import java.util.ArrayList;

import vcon.ConsistencyModel;
import vcon.VCon;
import vcon.VConInput;

public class ReadMyWrites {

	public ArrayList<String> getValidValues(VConInput input) {
		ArrayList<String> retList = null;
//		VConInput lastWrite = VCon.my_write.get(input.clientID).get(input.key);
//		if (lastWrite == null) {
//			for (VConInput in : VCon.history) {
//				if (in.opType.equals(ConsistencyModel.OpType.WR)) {
//					if (in.key.equals(input.key)) {
//						if (retList == null) {
//							retList = new ArrayList<String>();
//						}
//						retList.add(in.val);
//					}
//				}
//			}
//		} else {
//			boolean sawLastWrite = false;
//			for (VConInput in : VCon.history) {
//				if (in.opType.equals(ConsistencyModel.OpType.WR)) {
//					if (in.equals(lastWrite)) {
//						sawLastWrite = true;
//					}
//					if (sawLastWrite) {
//						if (in.key.equals(input.key)) {
//							if (retList == null) {
//								retList = new ArrayList<String>();
//							}
//							retList.add(in.val);
//						}
//					}
//				}
//			}
//		}
		return retList;
	}

}
