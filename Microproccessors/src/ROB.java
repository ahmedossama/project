public class ROB {
	String type;
	Register destination_register;
	int destination_memory;
	int value;
	boolean isReady;
	Instruction instruction;
	

	public ROB(String type, Register destination_register, int destination_memory, int value) {
		this.type = type;
		this.destination_memory = destination_memory;
		this.destination_register = destination_register;
		this.value = value;
		
	}

	public ROB() {

	}

}
