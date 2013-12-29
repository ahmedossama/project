import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Simulator {
	int memory_access_time;
	ArrayList<Cache> caches = new ArrayList<Cache>(3);
	int instuctionBufferSize;
	Map<String, reservationStation> reservationStations = new HashMap<String, reservationStation>();
	ROB[] reOrderBuffers;
	Register R0;
	Register R1;
	Register R2;
	Register R3;
	Register R4;
	Register R5;
	Register R6;
	Register R7;
	int cachelvls;
	int startAddress;
	int currentAddress;
	Register[] registerFile = new Register[8];
	Memory mem = new Memory();
	int numOfInstructions;
	Instruction[] instructionBuffer;
	String[] cacheInstruction;
	ArrayList<Instruction> instructions_array = new ArrayList<Instruction>();

	public Simulator() {
		for (int i = 0; i < 3; i++) {
			caches.add(null);
		}
	}

	public static void main(String[] args) throws Exception {
		Simulator simulator = new Simulator();
		BufferedReader br = new BufferedReader(new FileReader(
				"C:\\Users\\HGezeery\\project\\Microproccessors\\file.txt"));
		String everything;
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append('\n');
				line = br.readLine();
			}
			everything = sb.toString();
		} finally {
			br.close();
		}
		String[] instructions = everything.split("\n");

		// caches initialization
		int cache_lvls = Integer.parseInt(instructions[0]);
		int current_line = 2;
		String cache_1_geometry = instructions[1];
		if (cache_lvls == 2) {
			current_line = 3;
		}
		if (cache_lvls == 3) {
			current_line = 4;
		}

		switch (cache_lvls) {
		case 3:
			String[] cache_3_geometry1 = instructions[3].split(",");

			Cache lvl3 = new Cache(Integer.parseInt(cache_3_geometry1[0]),
					Integer.parseInt(cache_3_geometry1[1]),
					Integer.parseInt(cache_3_geometry1[2]),
					Integer.parseInt(cache_3_geometry1[3]),
					cache_3_geometry1[4], cache_3_geometry1[5]);
			simulator.caches.add(2, lvl3);
		case 2:

			String[] cache_2_geometry1 = instructions[2].split(",");

			Cache lvl2 = new Cache(Integer.parseInt(cache_2_geometry1[0]),
					Integer.parseInt(cache_2_geometry1[1]),
					Integer.parseInt(cache_2_geometry1[2]),
					Integer.parseInt(cache_2_geometry1[3]),
					cache_2_geometry1[4], cache_2_geometry1[5]);
			simulator.caches.add(1, lvl2);
		case 1:
			String[] cache_1_geometry1 = cache_1_geometry.split(",");

			Cache lvl1 = new Cache(Integer.parseInt(cache_1_geometry1[0]),
					Integer.parseInt(cache_1_geometry1[1]),
					Integer.parseInt(cache_1_geometry1[2]),
					Integer.parseInt(cache_1_geometry1[3]),
					cache_1_geometry1[4], cache_1_geometry1[5]);
			simulator.caches.add(0, lvl1);
		}
		simulator.cachelvls = simulator.caches.size() - 3;
		// memory access time
		simulator.memory_access_time = Integer
				.parseInt(instructions[current_line]);
		current_line++;

		// instruction buffer size
		simulator.instuctionBufferSize = Integer
				.parseInt(instructions[current_line]);
		current_line++;

		// Reservation stations initialization
		String reservation = instructions[current_line];
		current_line++;
		String[] reservations = reservation.split(",");
		int num_add = Integer.parseInt(reservations[0]);
		int num_mul = Integer.parseInt(reservations[1]);
		int num_load = Integer.parseInt(reservations[2]);
		for (int i = 1; i <= num_add; i++) {
			simulator.reservationStations.put("add" + i,
					new reservationStation("add" + i));
		}
		for (int i = 1; i <= num_mul; i++) {
			simulator.reservationStations.put("mul" + i,
					new reservationStation("mul" + i));
		}
		for (int i = 1; i <= num_load; i++) {
			simulator.reservationStations.put("load" + i,
					new reservationStation("load" + i));
		}
		// registers initialization
		simulator.R0 = new Register();
		simulator.R1 = new Register();
		simulator.R2 = new Register();
		simulator.R3 = new Register();
		simulator.R4 = new Register();
		simulator.R5 = new Register();
		simulator.R6 = new Register();
		simulator.R7 = new Register();
		simulator.registerFile[0] = simulator.R0;
		simulator.registerFile[1] = simulator.R1;
		simulator.registerFile[2] = simulator.R2;
		simulator.registerFile[3] = simulator.R3;
		simulator.registerFile[4] = simulator.R4;
		simulator.registerFile[5] = simulator.R5;
		simulator.registerFile[6] = simulator.R6;
		simulator.registerFile[7] = simulator.R7;

		// ROB initialization
		int num_ROB = Integer.parseInt(instructions[current_line]);
		current_line++;
		simulator.reOrderBuffers = new ROB[num_ROB];

		// start address
		simulator.startAddress = Integer.parseInt(instructions[current_line]);
		simulator.currentAddress = simulator.startAddress;
		current_line++;
		// look for data
		while (!instructions[current_line].equalsIgnoreCase("start")) {
			String data = instructions[current_line];
			if (data.charAt(0) == 'R') {
				String[] data_split = data.split(",");
				int index = Character.getNumericValue(data_split[0].charAt(1));
				int value = Integer.parseInt(data_split[1]); // data int or
																// string??
				simulator.registerFile[index].setValue(value);
			} else {
				String[] data_split = data.split(",");
				String value = data_split[1]; // data int or string??
				int datamem = Integer.parseInt(data_split[0].substring(4,
						data_split[0].length() - 1));
				simulator.mem.memory[datamem] = value;
			}
			current_line++;
		}
		current_line++;
		for (int i = current_line; i < instructions.length; i++) {
			simulator.mem.memory[simulator.currentAddress] = instructions[current_line];
			simulator.instructions_array.add(decode(simulator,
					instructions[current_line]));
			current_line++;
			simulator.currentAddress++;
			simulator.numOfInstructions++;
		}
		simulator.currentAddress = simulator.startAddress;
		simulator.cacheInstruction = new String[simulator.numOfInstructions];// correct
																				// size
																				// ??

		for (int i = 0; i < simulator.numOfInstructions; i++) {
			simulator.cacheInstruction[i] = simulator.mem.memory[simulator.currentAddress];
			simulator.currentAddress++;
		}
		simulator.instructionBuffer = new Instruction[simulator.instuctionBufferSize];
	}

	public static Instruction decode(Simulator s, String instruction) {
		String[] instruction_split = instruction.split(",");
		String operation = instruction_split[0];
		Instruction current = new Instruction();
		if (operation.equals("add") || operation.equals("sub")
				|| operation.equals("addi")) {
			current.type = "add";
			current.operation = operation;
			// Ra
			String register_a = instruction_split[1];
			char index = register_a.charAt(1);
			int reg_num = Character.getNumericValue(index);
			current.Ra = s.registerFile[reg_num];
			// Rb
			String register_b = instruction_split[2];
			char index_b = register_b.charAt(1);
			int reg_num_b = Character.getNumericValue(index_b);
			current.Rb = s.registerFile[reg_num_b];

			if (!operation.equals("addi")) {
				// Rc
				String register_c = instruction_split[3];
				char index_c = register_c.charAt(1);
				int reg_num_c = Character.getNumericValue(index_c);
				current.Rc = s.registerFile[reg_num_c];
			} else {
				// immediate
				String value_string = instruction_split[3];
				int value = Integer.parseInt(value_string);
				current.imm = value;
				current.immediate = true;
			}

		}

		if (operation.equals("mul") || operation.equals("nand")) {
			current.type = "mul";
			current.operation = operation;
			// Ra
			String register_a = instruction_split[1];
			char index = register_a.charAt(1);
			int reg_num = Character.getNumericValue(index);
			current.Ra = s.registerFile[reg_num];
			// Rb
			String register_b = instruction_split[2];
			char index_b = register_b.charAt(1);
			int reg_num_b = Character.getNumericValue(index_b);
			current.Rb = s.registerFile[reg_num_b];
			// Rc
			String register_c = instruction_split[3];
			char index_c = register_c.charAt(1);
			int reg_num_c = Character.getNumericValue(index_c);
			current.Rc = s.registerFile[reg_num_c];
		}

		if (operation.equals("lw") || operation.equals("sw")) {
			current.type = "load";
			current.operation = operation;
			// Ra
			String register_a = instruction_split[1];
			char index = register_a.charAt(1);
			int reg_num = Character.getNumericValue(index);
			current.Ra = s.registerFile[reg_num];
			// Rb
			String register_b = instruction_split[2];
			char index_b = register_b.charAt(1);
			int reg_num_b = Character.getNumericValue(index_b);
			current.Rb = s.registerFile[reg_num_b];
			// immediate
			String value_string = instruction_split[3];
			int value = Integer.parseInt(value_string);
			current.imm = value;
			current.immediate = true;
		}

		if (operation.equals("jmp")) {
			current.type = "unknown";// ??
			current.operation = operation;
			// Ra
			String register_a = instruction_split[1];
			char index = register_a.charAt(1);
			int reg_num = Character.getNumericValue(index);
			current.Ra = s.registerFile[reg_num];
			// immediate
			String value_string = instruction_split[2];
			int value = Integer.parseInt(value_string);
			current.imm = value;
			current.immediate = true;
		}

		if (operation.equals("beq")) {
			current.type = "unknpwn";
			current.operation = operation;
			// Ra
			String register_a = instruction_split[1];
			char index = register_a.charAt(1);
			int reg_num = Character.getNumericValue(index);
			current.Ra = s.registerFile[reg_num];
			// Rb
			String register_b = instruction_split[2];
			char index_b = register_b.charAt(1);
			int reg_num_b = Character.getNumericValue(index_b);
			current.Rb = s.registerFile[reg_num_b];
			// immediate
			String value_string = instruction_split[3];
			int value = Integer.parseInt(value_string);
			current.imm = value;
			current.immediate = true;
		}

		if (operation.equals("jalr")) {
			current.type = "unknpwn";
			current.operation = operation;
			// Ra
			String register_a = instruction_split[1];
			char index = register_a.charAt(1);
			int reg_num = Character.getNumericValue(index);
			current.Ra = s.registerFile[reg_num];
			// Rb
			String register_b = instruction_split[2];
			char index_b = register_b.charAt(1);
			int reg_num_b = Character.getNumericValue(index_b);
			current.Rb = s.registerFile[reg_num_b];
		}

		if (operation.equals("ret")) {
			current.type = "unknpwn";
			current.operation = operation;
			// Ra
			String register_a = instruction_split[1];
			char index = register_a.charAt(1);
			int reg_num = Character.getNumericValue(index);
			current.Ra = s.registerFile[reg_num];
		}
		return current;

	}

}
