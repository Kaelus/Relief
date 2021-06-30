package vcon.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import vcon.ConsistencyModel;
import vcon.VCon;
import vcon.VConInput;

public class demo1 {

	public static void main(String[] args) {
		ArrayList<ConsistencyModel.CM> cm = new ArrayList<ConsistencyModel.CM>();
		cm.add(ConsistencyModel.CM.SC);
		
		VCon oracle = new VCon();
		
		File traceFile = new File("experiment/trace.sample");
		try (BufferedReader br = new BufferedReader(new FileReader(traceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
            	System.out.println("line is " + line);
            	VConInput vcin = null;
            	if (line.startsWith("#")) {
            		// commented line. skip.
            		continue;
            	} else {
            		String[] inputTokens = line.split(",");
            		String clientID = inputTokens[0].split("=")[1];
            		String key = inputTokens[1].split("=")[1];
            		String opTypeStr = inputTokens[2].split("=")[1];
            		String val = inputTokens[3].split("=")[1];
            		String tsStr = inputTokens[4].split("=")[1];
            		ConsistencyModel.OpType opType = null;
            		if (opTypeStr.equals("W")) {
            			opType = ConsistencyModel.OpType.WR;
            		} else if (opTypeStr.equals("R")) {
            			opType = ConsistencyModel.OpType.RD;
            		} else {
            			System.err.println("Unknown op type=" + opTypeStr);
            			System.exit(1);
            		}
            		long ts = Long.parseLong(tsStr);
            		vcin = new VConInput(clientID, key, opType,
            				val, ts);
            		if (vcin.opType.equals(ConsistencyModel.OpType.RD)) {
                		VConInput queryvcin = new VConInput(clientID, key, opType, null, ts);
                		ArrayList<String> validValues = oracle.query(queryvcin, cm);
                		if (validValues == null) {
                			System.err.println("validVaules should not be null");
                		} else {
                			System.out.println("validValues contains:");
                			System.out.println(validValues.toArray());
                		}
                		boolean isValid = false;
                		for (String vv : validValues) {
                			if (vv == null) {
                				System.err.println("validValues should not contain null");
                			}
                			if (vv.equals(vcin.val)) {
                				isValid = true;
                				break;
                			}
                		}
                		if (!isValid) {
                			System.out.println("!!! VIOLATION DETECTED !!!");
                			break;
                		} else {
                			System.out.println("Value is VALID");
                		}
                	} 
                	oracle.update(vcin, cm);
            	}
            } 
            System.out.println("Verification PASSED");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block                      
            e.printStackTrace();
        } catch (IOException e) {
        	// TODO Auto-generated catch block                      
            e.printStackTrace();
        }
		
	}
	
}
