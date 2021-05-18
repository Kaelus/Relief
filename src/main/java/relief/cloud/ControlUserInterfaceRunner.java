package relief.cloud;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ControlUserInterfaceRunner implements Runnable {

	String loggerID = "ControlUserInterfaceRunner";
	boolean quitFlag = false;
	
	// command constants
	//private final int CMD_QUIT = -1;
	//private final int CMD_CLEAR_HISTORY = 1;
	//private final int CMD_CLEAR_DATA = 2;
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
			System.out.println("[" + loggerID + "] What do you want to do? "
							+ "[-1] quit [1] clear history [2] clear data"
							+ "\n");
			while (!quitFlag && ((input = br.readLine()) != null)) {
				try {
					int cmd = Integer.valueOf(input);
					System.out.println("[" + loggerID + "] cmd=" + cmd); 
					switch (cmd) {
					case -1:
						quitFlag = true;
						break;
					case 1:
						ReliefController.historyManager.clear();
						break;
					case 2:
						ReliefController.dataManager.clear();
						break;
					default:
						break;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (!quitFlag) {
					System.out.println("[" + loggerID + "] What do you want to do? "
							+ "[-1] quit [1] clear history [2] clear data"
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
