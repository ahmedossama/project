import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Simulator {
	int cycles = 0;
	int memory_access_time;
	Cache[] caches;
	int instuctionBufferSize;
	reservationStation[] reservationStations;
	ROB[] reOrderBuffers;
	Register R0;
	Register R1;
	Register R2;
	Register R3;
	Register R4;
	Register R5;
	Register R6;
	Register R7;
	Register PC;
	int cachelvls;
	int startAddress;
	int currentAddress;
	int instructionsInBuffer = 0;
	int instructionBufferIndex = 0;
	int ROB_head;
	int ROB_tail;
	boolean fetchHazard = false;
	Register[] registerFile = new Register[9];
	Memory mem = new Memory();
	int numOfInstructions;
	Instruction[] instructionBuffer;
	String[] cacheInstruction;
	FunctionalUnit[] functionalUnits;
	ArrayList<Instruction> instructions_array = new ArrayList<Instruction>();

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
		simulator.caches = new Cache[cache_lvls];
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
			simulator.caches[2] = lvl3;
		case 2:

			String[] cache_2_geometry1 = instructions[2].split(",");

			Cache lvl2 = new Cache(Integer.parseInt(cache_2_geometry1[0]),
					Integer.parseInt(cache_2_geometry1[1]),
					Integer.parseInt(cache_2_geometry1[2]),
					Integer.parseInt(cache_2_geometry1[3]),
					cache_2_geometry1[4], cache_2_geometry1[5]);
			simulator.caches[1] = lvl2;
		case 1:
			String[] cache_1_geometry1 = cache_1_geometry.split(",");

			Cache lvl1 = new Cache(Integer.parseInt(cache_1_geometry1[0]),
					Integer.parseInt(cache_1_geometry1[1]),
					Integer.parseInt(cache_1_geometry1[2]),
					Integer.parseInt(cache_1_geometry1[3]),
					cache_1_geometry1[4], cache_1_geometry1[5]);
			simulator.caches[0] = lvl1;
		}
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
		simulator.reservationStations = new reservationStation[num_add
				+ num_mul + num_load];
		int x;
		for (int i = 0; i < num_add + num_mul + num_load; i++) {
			if (i < num_add) {
				simulator.reservationStations[i] = new reservationStation("add"
						+ i);
			} else {
				if (i < num_mul + num_add) {
					x = i - num_add;
					simulator.reservationStations[i] = new reservationStation(
							"mul" + (i - num_add));
				} else {
					if (i < num_load + num_add + num_mul) {
						x = i - num_add - num_mul;
						simulator.reservationStations[i] = new reservationStation(
								"load" + (i - num_add - num_mul));
					}
				}
			}

		}

		// Functional Unit initialization
		simulator.functionalUnits = new FunctionalUnit[num_add + num_mul
				+ num_load];
		String functionalUnitName = "";
		for (int i = 0; i < num_add + num_mul + num_load; i++) {
			functionalUnitName = simulator.reservationStations[i].type;
			simulator.functionalUnits[i] = new FunctionalUnit(
					functionalUnitName, new Register());
		}

		// registers initialization
		simulator.R0 = new Register();
		simulator.R1 = new Register();
		simulator.R2 = new Register();
		simulator.R3 = new Register();
		simulator.R3.setValue(3);
		simulator.R4 = new Register();
		simulator.R5 = new Register();
		simulator.R6 = new Register();
		simulator.R7 = new Register();
		simulator.PC = new Register();
		simulator.registerFile[0] = simulator.R0;
		simulator.registerFile[1] = simulator.R1;
		simulator.registerFile[2] = simulator.R2;
		simulator.registerFile[3] = simulator.R3;
		simulator.registerFile[4] = simulator.R4;
		simulator.registerFile[5] = simulator.R5;
		simulator.registerFile[6] = simulator.R6;
		simulator.registerFile[7] = simulator.R7;
		simulator.registerFile[8] = simulator.PC;

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
		// getting the instructions from the text file
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
		for (int i = 0; i < simulator.instuctionBufferSize; i++)
			simulator.instructionBuffer[i] = new Instruction();
		simulator.currentAddress = simulator.startAddress;
		simulate(simulator);
	}

	public static Instruction decode(Simulator s, String instruction) {
		String[] instruction_split = instruction.split(",");
		String operation = instruction_split[0];
		Instruction current = new Instruction();
		current.status = "";
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

	public static int computeArithmetic(Instruction instruction) {
		if (instruction.operation.equals("add"))
			return instruction.Rb.getValue() + instruction.Rc.getValue();
		if (instruction.operation.equals("addi"))
			return instruction.Rb.getValue() + instruction.imm;
		if (instruction.operation.equals("sub"))
			return instruction.Rb.getValue() - instruction.Rc.getValue();
		if (instruction.operation.equals("mul"))
			return instruction.Rb.getValue() * instruction.Rc.getValue();
		if (instruction.operation.equals("nand")) {
			int value = instruction.Rb.getValue() & instruction.Rc.getValue();
			return intComplement(value);
		}
		return 0;

	}

	public static int intComplement(int decimal) {
		int base = 2;
		int result = 0;
		int multiplier = 1;

		while (decimal > 0) {
			int residue = decimal % base;
			decimal = decimal / base;
			result = result + residue * multiplier;
			multiplier = multiplier * 10;
		}
		String result1 = result + "";
		String result2 = "";
		for (int i = 0; i < result1.length(); i++) {
			if (result1.charAt(i) == '1') {
				result2 += '0';
			}
			if (result1.charAt(i) == '0') {
				result2 += '1';
			}
		}
		return Integer.parseInt(result2, 2);
	}

	public static void simulate(Simulator s) {
		s.PC.setValue(s.startAddress);

		while (s.PC.getValue() < (s.startAddress + s.numOfInstructions)
				|| s.ROB_head != s.ROB_tail || s.cycles==1) {
			// instructions still exist || or the ROB entry at head is not null
			// continue
			System.out.println(s.ROB_head);
			System.out.println(s.ROB_tail);
			s.cycles++;
			if (s.PC.getValue() != s.startAddress) { // for the first
														// instruction
				s.fetchHazard = false;
				// commit
				if (s.reOrderBuffers[s.ROB_head] != null
						&& s.reOrderBuffers[s.ROB_head].isReady) {
					// if destination is not memory
					if (s.reOrderBuffers[s.ROB_head].destination_memory == -1) {
						s.reOrderBuffers[s.ROB_head].destination_register
								.setValue(s.reOrderBuffers[s.ROB_head].value);
					} else {
						s.mem.memory[s.reOrderBuffers[s.ROB_head].destination_memory] = s.reOrderBuffers[s.ROB_head].value
								+ "";
					}
					s.reOrderBuffers[s.ROB_head] = null;
					s.ROB_head++;
					if (s.ROB_head >= s.reOrderBuffers.length)
						s.ROB_head = 0;
				}

				// write-back
				String opr;
				for (int i = 0; i < s.reservationStations.length; i++) {
					if (s.reservationStations[i].busy) {
						if (s.reservationStations[i].remaining_cycles == 0) {
							s.reOrderBuffers[s.reservationStations[i].destination].isReady = true;
							s.reOrderBuffers[s.reservationStations[i].destination].value = computeArithmetic(s.reservationStations[i].instruction);
							s.reservationStations[i].busy = false;
							opr = s.reservationStations[i].type;
							for (int j = 0; j < s.reservationStations.length; j++) {
								if (s.reservationStations[j].busy) {
									if(s.reservationStations[j].qj.equals(opr))
									s.reservationStations[j].rj = 1;
								}
								if(s.reservationStations[j].busy){
								if (s.reservationStations[j].qk.equals(opr)) {
									s.reservationStations[j].rk = 1;
								}
								}
							}
						}

					}
				}
				// execute
				for (int i = 0; i < s.reservationStations.length; i++) {
					if (s.reservationStations[i].busy
							&& s.reservationStations[i].rj == 1
							&& s.reservationStations[i].rk == 1) {
						s.reservationStations[i].remaining_cycles--;
					}

				}
				// issue
				// if operation not store i.e destination not memory, register
				// r
				if (s.instructionBuffer[0] != null
						&& (s.instructionBuffer[0].type.equals("add") || s.instructionBuffer[0].type
								.equals("mul"))) {
					if (s.reOrderBuffers[s.ROB_tail] != null)
						s.fetchHazard = true;

					if (!s.fetchHazard) {
						reservationSettingLoop: for (int i = 0; i < s.reservationStations.length; i++) {
							if ((s.reservationStations[i].type.substring(0,
									s.reservationStations[i].type.length() - 1)
									.equals(s.instructionBuffer[0].type))
									&& (!s.reservationStations[i].busy)) {
								s.reservationStations[i].busy = true;
								s.reservationStations[i].operation = s.instructionBuffer[0].operation;
								s.functionalUnits[i].busy = true;
								s.functionalUnits[i].register = s.instructionBuffer[0].Ra;
								if (s.instructionBuffer[0].type.equals("add")) {
									s.reservationStations[i].remaining_cycles = 2;
								} else if (s.instructionBuffer[0].type
										.equals("mul")) {
									s.reservationStations[i].remaining_cycles = 6;
								}

								s.reOrderBuffers[s.ROB_tail] = new ROB(
										s.instructionBuffer[0].type,
										s.instructionBuffer[0].Ra, -1, 0);
								System.out
										.println(s.reOrderBuffers[s.ROB_head]);
								s.reservationStations[i].destination = s.ROB_tail;
								s.ROB_tail++;
								if (s.ROB_tail == s.reOrderBuffers.length)
									s.ROB_tail = 0;
								// get rj rk qj qk
								s.reservationStations[i].rj = 1;
								s.reservationStations[i].rk = 1;
								s.reservationStations[i].qj="";
								s.reservationStations[i].qk="";
								for (int j = 0; j < s.functionalUnits.length; j++) {
									if (s.functionalUnits[j].register
											.equals(s.instructionBuffer[0].Rb)) {
										s.reservationStations[i].rj = 0;
										s.reservationStations[i].qj = s.functionalUnits[j].name;
									}

									if (s.functionalUnits[j].register
											.equals(s.instructionBuffer[0].Rc)) {
										s.reservationStations[i].rk = 0;
										s.reservationStations[i].qk = s.functionalUnits[j].name;
									}

								}
								// send instruction to reservation station
								s.reservationStations[i].instruction = s.instructionBuffer[0];
								s.instructionBuffer[0] = null;
								s.instructionBufferIndex--;
								s.instructionsInBuffer--;
								break reservationSettingLoop;
							}
						}
						// shifting instruction buffer
						if (s.instructionBuffer[0] == null) {
							for (int i = 1; i <= s.instructionsInBuffer; i++) {
								s.instructionBuffer[i - 1] = s.instructionBuffer[i];
							}
						}
					}
				}
			}
			// fetch
			if (s.instructionBufferIndex < s.instructionBuffer.length) {
				if (s.PC.getValue() < s.startAddress + s.numOfInstructions) {
					s.instructionBuffer[s.instructionBufferIndex] = decode(s,
							s.mem.memory[s.PC.getValue()]);
					System.out.println(s.instructionBuffer[0].type);
					s.instructionBuffer[s.instructionBufferIndex].status = "fetched";
					s.instructionBufferIndex++;
					s.instructionsInBuffer++;
					s.PC.setValue(s.PC.getValue() + 1);
				}
			}

		}
		System.out.println("no. of cycles = " + s.cycles + "");
		System.out.println("R4 value = " + s.R4.getValue());

	}
}
