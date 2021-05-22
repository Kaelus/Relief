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
		boolean skipFlag = false;
		for (int i = 1; i < args.length; i++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			String arg = args[i];
			if (arg.equals("-silent")) {
				System.out.println("Running the YCSB in silent mode..");
				DebugLog.VERBOSE = false;
			} else if (arg.equals("-r")) {
				skipFlag = true;
			} else {
				filteredArrayList.add(arg);
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
