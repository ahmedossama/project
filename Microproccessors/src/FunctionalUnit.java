
public class FunctionalUnit {
	
	String name;
	Register register;
	boolean busy;
	int memoryIndex;
	
	public FunctionalUnit(String name, Register register){
		this.name=name;
		this.register = register;
	}
}
