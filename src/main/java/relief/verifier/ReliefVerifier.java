package relief.verifier;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import relief.util.DebugLog;
import vcon.ConsistencyModel;
import vcon.VCon;
import vcon.VConInput;

public class ReliefVerifier {

	ArrayList<ConsistencyModel.CM> cm;
	String traceFileName;
	VCon oracle;

	public ReliefVerifier(String configFile) {
		cm = new ArrayList<ConsistencyModel.CM>();
		traceFileName = null;
		parseConfigInit(configFile);
		oracle = new VCon();
	}

	private void parseConfigInit(String configFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("consistencyModels")) {
					String[] tokens = line.split("=");
					String consistencyModelsStr = tokens[1];
					DebugLog.log("consistencyModelsStr=" + consistencyModelsStr);
					String[] consistencyModelsArr = consistencyModelsStr.split(",");
					DebugLog.log("consistencyModelsArr=" + Arrays.toString(consistencyModelsArr));
					for (String strToken : consistencyModelsArr) {
						if (strToken.equals(ConsistencyModel.CM.SC.toString())) {
							cm.add(ConsistencyModel.CM.SC);
						} else if (strToken.equals(ConsistencyModel.CM.EC.toString())) {
							cm.add(ConsistencyModel.CM.EC);
						} else if (strToken.equals(ConsistencyModel.CM.BS.toString())) {
							cm.add(ConsistencyModel.CM.BS);
						} else if (strToken.equals(ConsistencyModel.CM.MR.toString())) {
							cm.add(ConsistencyModel.CM.MR);
						} else if (strToken.equals(ConsistencyModel.CM.RM.toString())) {
							cm.add(ConsistencyModel.CM.RM);
						}
					}
				} else if (line.startsWith("traceFileName")) {
					String[] tokens = line.split("=");
					traceFileName = tokens[1];
					DebugLog.log("traceFileName=" + traceFileName);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runVerification() {
		File traceFile = new File(traceFileName);
		if (!traceFile.exists()) {
			System.err.println("Unable to find " + traceFileName);
            System.exit(1);
		}
		
		//try(BufferedReader br = new BufferedReader(new FileReader(traceFile))) {
		//	String line;
		//	while ((line = br.readLine()) != null) {
		//		DebugLog.log("line is " + line);
		//		VConInput vcin = null;
		//		if (line.startsWith("#")) {
		//			// commented line. skip.
		//			continue;
		//		} else {
//					String[] inputTokens = line.split(",");
//					String clientID = inputTokens[0].split("=")[1];
//					String key = inputTokens[1].split("=")[1];
//					String opTypeStr = inputTokens[2].split("=")[1];
//					String val = inputTokens[3].split("=")[1];
//					String tsStr = inputTokens[4].split("=")[1];
//					ConsistencyModel.OpType opType = null;
//					if (opTypeStr.equals("W")) {
//						opType = ConsistencyModel.OpType.WR;
//					} else if (opTypeStr.equals("R")) {
//						opType = ConsistencyModel.OpType.RD;
//					} else {
//						System.err.println("Unknown op type=" + opTypeStr);
//						System.exit(1);
//					}
//					long ts = Long.parseLong(tsStr);
//					vcin = new VConInput(clientID, key, opType, val, ts);
		int opCount = 0;
		long startTime = System.currentTimeMillis();
		try {
			FileInputStream fileIn = new FileInputStream(traceFile);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			VConInput vcin = null;
			while((vcin = (VConInput) objectIn.readObject()) != null) {
				opCount++;
				DebugLog.log("vcin=" + vcin.toString());
				if (vcin.opType.equals(ConsistencyModel.OpType.RD)) {
					//VConInput queryvcin = new VConInput(clientID, key, opType, null, ts);
					VConInput queryvcin = new VConInput(vcin.clientID, vcin.key, vcin.opType, vcin.val, vcin.timestamp);
					ArrayList<String> validValues = oracle.query(queryvcin, cm);
					boolean isValid = false;
					if (validValues == null) {
						DebugLog.log("validVaules is null. The key has never been written before");
						if (vcin.val.equals("null")) {
							isValid = true;
						}
					} else {
						DebugLog.log("validValues contains:" + Arrays.toString(validValues.toArray()));
						for (String vv : validValues) {
							if (vv == null) {
								DebugLog.log("validValues should not contain null");
							}
							if (vv.equals(vcin.val)) {
								isValid = true;
								break;
							}
						}
					}
					if (!isValid) {
						DebugLog.log("!!! VIOLATION DETECTED !!!");
						throw new EOFException();
						//break;
					} else {
						DebugLog.log("Value is VALID");
					}
				}
				oracle.update(vcin, cm);
			}
			objectIn.close();
			fileIn.close();
			DebugLog.log("Verification PASSED");
		} catch (EOFException e) {
			e.printStackTrace();
			DebugLog.log("opCount=" + opCount);
			long endTime = System.currentTimeMillis();
			long cumTime = endTime - startTime;
			DebugLog.log("time taken(ms)=" + cumTime);
			DebugLog.log("norm. time taken (normalized to 1000 ops)=" + (cumTime / (double)opCount * 1.0) * 1000 ) ;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (ClassNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String configFile = null;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-c") && i < args.length - 1) {
				DebugLog.log("configuration file is given=" + args[i + 1]);
				configFile = args[i + 1];
			}
		}
		if (configFile == null) {
			System.err.println("ERROR configFile is not given..");
			System.exit(1);
		}
		ReliefVerifier verifier = new ReliefVerifier(configFile);
		verifier.runVerification();
	}

}

