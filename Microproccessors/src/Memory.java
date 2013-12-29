public class Memory {
	String[] memory;

	public Memory() {
		memory = new String[65536];
	}

	public String getElementByAddress(int address) {
		return memory[address];
	}

	public void setElementByAddress(int address, String element) {
		memory[address] = element;
	}
	
}
