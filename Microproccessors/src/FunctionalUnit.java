
public class FunctionalUnit {
	
	String name;
	Register register;
	boolean busy;
	
	public FunctionalUnit(String name, Register register){
		this.name=name;
		this.register = register;
	}
}
