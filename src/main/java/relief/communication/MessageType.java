package relief.communication;

/*
 * Message Type Constants defined here.
 *
 */
public class MessageType {

	public static final int MSG_T_UNDEFINED = -1;
	public static final int MSG_T_ACK = 0;
	public static final int MSG_T_REGISTER_CLIENT = 1;
	public static final int MSG_T_GET = 2;
	public static final int MSG_T_PUT = 3;
	public static final int MSG_T_READ_HIST = 4;
	public static final int MSG_T_WRITE_ATTEST = 5;
	public static final int MSG_T_READ_ATTEST = 6;
	public static final int MSG_T_SCH_UP_FORWARD = 7;

	
	// old
	public static final int MSG_T_UP = 100;
	public static final int MSG_T_FETCH = 102;
	public static final int MSG_T_FETCH_RESP = 103;
	public static final int MSG_T_VALUE_UP = 104;
	public static final int MSG_T_VALUE_DW = 105;
	
	public static final int MSG_T_PUT_EC = 107;
	public static final int MSG_T_GET_EC = 108;
	public static final int MSG_T_PUT_SC = 109;
	public static final int MSG_T_GET_SC = 1010;
	public static final int MSG_T_PUT_CC = 1011;
	public static final int MSG_T_GET_CC = 1012;
	public static final int MSG_T_WPUT_EC = 1013;
	public static final int MSG_T_WGET_EC = 1014;
	
	// Update Type Constants. HB=heartbeat, CU=client update, WU=watcher update
	public static final int UP_T_HB = 0;
	public static final int UP_T_CU = 1;
	public static final int UP_T_WU = 2;
	
	
}

