package vcon;

public class ConsistencyModel {

	/*
	 * Consistency Models: SC(Strong Consistency) EC(Eventual Consistency)
	 * BS(Bounded Staleness) MR(Monotonic Reads) RM(Read-My-Write)
	 */
	public enum CM {
		SC, EC, BS, MR, RM
	};

	// Event type, either request or response
	public enum OpType {
		WR, RD
	};

	
}
