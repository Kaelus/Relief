package com.relief.ycsb.driver;

import java.util.ArrayList;
import java.util.List;

import com.yahoo.ycsb.Client;

import relief.util.DebugLog;

public class ReliefYCSBDriver {
	public static void main(String[] args) {
		String[] filteredArgs;
		List<String> filteredArrayList = new ArrayList<String>();
		if (args.length > 0) {
			int j=0;
			for (String s:args) {
				System.out.println("argv["+j+"]=" + args[j++]);
			}
		}
		int i=0;
		for (String s : args) {
			if (s.equals("silent")) {
				System.out.println("Running the YCSB in silent mode..");
				DebugLog.VERBOSE = false;
			} else {
				filteredArrayList.add(s);
			}
		}
		filteredArgs = filteredArrayList.toArray(new String[filteredArrayList.size()]);
		Client.main(filteredArgs);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//DebugLog.log("statistics: " + UnityClientMeasurements.getOutputString());
		//System.out.println("statistics: " + UnityClientMeasurements.getOutputString());
	}
}
