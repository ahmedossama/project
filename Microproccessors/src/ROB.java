public class ROB {
	String type;
	Register destination_register;
	int destination_memory;
	int value;
	boolean isReady;
	

	public ROB(String type) {
		this.type = type;
	}

	public ROB() {

	}

}
