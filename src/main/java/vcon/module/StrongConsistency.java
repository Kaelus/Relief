package vcon.module;

import java.io.IOException;
import java.util.ArrayList;

import relief.util.GenericKeyValueStore;
import relief.util.ObjectSerializer;
import relief.util.ValueStorageLevelDB;
import vcon.ConsistencyModel;
import vcon.VCon;
import vcon.VConInput;

public class StrongConsistency {

	private GenericKeyValueStore scStore;
	private String scStoreName = "scStore";
	
	public StrongConsistency() {
		try {
			scStore = new ValueStorageLevelDB(scStoreName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update(VConInput input) {
		try {
			scStore.put(input.key, ObjectSerializer.serialize(input.val));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getValidValues(VConInput input) {
		ArrayList<String> retList = null;
		try {
			String valStr = (String) ObjectSerializer.deserialize(scStore.get(input.key));
			retList.add(valStr);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retList;
//		ArrayList<String> retList = null;
//		for (int i = VCon.glog.size()-1; i >=0; i--) {
//			System.out.println("checking...");
//			VConInput in = VCon.glog.get(i);
//			System.out.println("current VConInput in=" + in.toString());
//			System.out.println("input is " + input.toString());
//			if (in.opType.equals(ConsistencyModel.OpType.WR)) {
//				if (in.key.equals(input.key)) {
//					System.out.println("Found valid value for SC");
//					retList = new ArrayList<String>();
//					retList.add(in.val);
//					break;
//				}
//			}
//		}
//		return retList;
	}

}
