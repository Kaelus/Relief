package vcon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;
import relief.util.GenericKeyValueStore;
import relief.util.ObjectSerializer;
import relief.util.ValueStorageLevelDB;
import vcon.module.BoundedStaleness;
import vcon.module.EventualConsistency;
import vcon.module.MonotonicReads;
import vcon.module.ReadMyWrites;
import vcon.module.StrongConsistency;

public class VCon {

	// global log history
	//public static ArrayList<VConInput> glog;
	public static GenericKeyValueStore history;
	public static String historyFileName = "history";
	public static int sequenceNumber = 0;

	// MR session information: <ClientID> -> <Value>
	public static HashMap<String, String> last_read;

	// RM session information: <ClientID> -> <Key> -> <Input>
	public static HashMap<String, HashMap<String, VConInput>> my_write;

	// Modules for supported consistency models
	StrongConsistency sc;
	EventualConsistency ec;
	BoundedStaleness bs;
	MonotonicReads mr;
	ReadMyWrites rm;
	
	// time bound for bs in milliseconds
	public static long timebound;

	public VCon() {
		try {
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init() throws IOException {
		//glog = new ArrayList<VConInput>();
		history = new ValueStorageLevelDB(historyFileName);
		my_write = new HashMap<String, HashMap<String, VConInput>>();
		last_read = new HashMap<String, String>();
		sc = new StrongConsistency();
		ec = new EventualConsistency();
		bs = new BoundedStaleness();
		mr = new MonotonicReads();
		rm = new ReadMyWrites();
	}

	private <T> List<T> intersection(List<T> list1, List<T> list2) {
		if (list1 == null || list2 == null) {
			return null;
		}

		List<T> list = new ArrayList<T>();

		for (T t : list1) {
			if (list2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	public void update(VConInput input, ArrayList<ConsistencyModel.CM> cm) throws IOException {
		System.out.println("updating...input " + input.toString());
		//glog.add(input);
		history.put(sequenceNumber++ + "", ObjectSerializer.serialize(input));
		if (input.opType.equals(ConsistencyModel.OpType.WR)) {
			if (cm.contains(ConsistencyModel.CM.SC)) {
				sc.update(input);
			}
			if (cm.contains(ConsistencyModel.CM.RM)) {
				my_write.get(input.clientID).put(input.key, input);
			}
		} else if (input.opType.equals(ConsistencyModel.OpType.RD)) {
			if (cm.contains(ConsistencyModel.CM.MR)) {
				Assert.assertTrue("input val is null!", input.val != null);
				last_read.put(input.clientID, input.val);
			}
		}
	}

	public ArrayList<String> query(VConInput input,
			ArrayList<ConsistencyModel.CM> cm) {
		ArrayList<String> answer = null;
		ArrayList<String> retValues = null;

		Assert.assertTrue("You can call query only for RD", input.opType == ConsistencyModel.OpType.RD);
		if (input.val != null) {
			System.out.println("WARINING: Caution! you might be using query with read that sew null!");
		}
		
		if (cm.contains(ConsistencyModel.CM.SC)) {
			System.out.println("getting valid values for Strong Consistency");
			retValues = sc.getValidValues(input);
			System.out.println("Strong retValues=" + Arrays.toString(retValues.toArray()));
			if (answer == null) {
				answer = retValues;
			} else {
				answer = (ArrayList<String>) intersection(answer, retValues);
			}
			System.out.println("final answer=" + answer.toArray());
		}
		if (cm.contains(ConsistencyModel.CM.EC)) {
			System.out.println("getting valid values for Eventual Consistency");
			retValues = ec.getValidValues(input);
			if (answer == null) {
				answer = retValues;
			} else {
				answer = (ArrayList<String>) intersection(answer, retValues);
			}
		}
		if (cm.contains(ConsistencyModel.CM.BS)) {
			System.out.println("getting valid values for Bounded Staleness");
			retValues = bs.getValidValues(input);
			if (answer == null) {
				answer = retValues;
			} else {
				answer = (ArrayList<String>) intersection(answer, retValues);
			}
		}
		if (cm.contains(ConsistencyModel.CM.MR)) {
			//System.out.println("getting valid values for Monotonic Reads");
			//System.out.println("Querying for read VConInput=" + input.toString());
			retValues = mr.getValidValues(input);
			//System.out.println("monotonic reads module returns:");
			//for (String v : retValues) {
			//	System.out.println(v + "\n");
			//}
			if (answer == null) {
				answer = retValues;
			} else {
				answer = (ArrayList<String>) intersection(answer, retValues);
			}
			//System.out.println("after intersection in mr answer is:");
			//for (String v : answer) {
			//	System.out.println(v + "\n");
			//}
		}
		if (cm.contains(ConsistencyModel.CM.RM)) {
			System.out.println("getting valid values for Read My Write");
			retValues = rm.getValidValues(input);
			if (answer == null) {
				answer = retValues;
			} else {
				answer = (ArrayList<String>) intersection(answer, retValues);
			}
		}

		return answer;
	}

}
