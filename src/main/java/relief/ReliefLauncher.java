package relief;

import com.relief.ycsb.driver.ReliefYCSBDriver;

import relief.client.ReliefClient;
import relief.cloud.ReliefController;

public class ReliefLauncher {
	
	public enum AppType {ReliefServer, ReliefClient, ReliefYCSBDriver};

	public static void main(String[] args) throws Exception {
		String appName = "ReliefController";
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-r") && i < args.length - 1) {
				System.out.println("Given appName=" + args[i+1]);
				appName = args[i+1];
			}
		}
		if (appName.contentEquals(AppType.ReliefServer.toString())) {
			System.out.println("Run ReliefController.main");
			ReliefController.main(args);
		} else if (appName.contentEquals(AppType.ReliefClient.toString())) {
			System.out.println("Run ReliefClient.main");
			ReliefClient.main(args);
		} else if (appName.contentEquals(AppType.ReliefYCSBDriver.toString())) {
			System.out.println("Run ReliefYCSBDriver.main");
			ReliefYCSBDriver.main(args);
		} else {
			System.err.println("Unknown application type:" + appName);
			System.exit(1);
		}
	}
	
}
