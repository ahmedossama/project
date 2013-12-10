import java.util.*;

public class Parser {
	String[] inputArray;
	String instruction;
	String[] registers;

	public void parse() {
		while (true) {
			Scanner sc = new Scanner(System.in);
			System.out.print("Instruction:>> ");
			String input = sc.nextLine();
			// instruction = input.split(" ", 2);
			inputArray = input.split(" ", 2);
			String tmp = inputArray[1];
			instruction = inputArray[0];
			registers = tmp.split(",", 3);
			printArray();
		}
	}

	public void printArray() {
		System.out.print(instruction);
		System.out.println("");
		System.out.print(registers[0]);
		System.out.println("");
		System.out.print(registers[1]);
		System.out.println("");
		System.out.print(registers[2]);
		System.out.println("");
	}

	public static void main(String[] args) {
		Parser p = new Parser();
		p.parse();

	}
}
