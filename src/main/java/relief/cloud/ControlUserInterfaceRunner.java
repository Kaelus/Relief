package relief.cloud;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ControlUserInterfaceRunner implements Runnable {

	String loggerID = "ControlUserInterfaceRunner";
	boolean quitFlag = false;
	
	// command constants
	private final int CMD_QUIT = -1;
	public ReliefController reliefController;
	
	public ControlUserInterfaceRunner (ReliefController rc) {
		this.reliefController = rc;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("[ControlUserInterfaceRunner] entered run");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String input;
			System.out
					.println("[" + loggerID + "] What do you want to do? "
							+ "[" + CMD_QUIT + "] quit"
							+ "\n");
			while (!quitFlag && ((input = br.readLine()) != null)) {
				try {
					int cmd = Integer.valueOf(input);
					System.out.println("[" + loggerID + "] cmd=" + cmd); 
					switch (cmd) {
					case CMD_QUIT:
						quitFlag = true;
						break;
					default:
						break;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (!quitFlag) {
					System.out
					.println("[" + loggerID + "] What do you want to do? "
							+ "[" + CMD_QUIT + "] quit"
							+ "\n");
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			System.err.println("[" + loggerID + "] JavaClient: " + exception);
		}

		System.out.println("[" + loggerID + "] The main function is Done now...");
		System.out.println("[" + loggerID + "] Goodbye!!!");
	}

}
