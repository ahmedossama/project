import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Simulator {
	double cycles = 0;
	int x = 100;
	boolean isFound = false;
	int fetchCycles = 0;
	int branch_destination = -1;
	int memory_access_time;
	Cache[] caches;
	int instuctionBufferSize;
	reservationStation[] reservationStations;
	ROB[] reOrderBuffers;
	boolean false_branch_prediction;
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
	boolean isFetching = false;
	boolean fetchHazard = false;
	Register[] registerFile = new Register[9];
	Memory mem = new Memory();
	double numOfInstructions;
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
		simulator.caches = new Cache[cache_lvls + 1];
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
			simulator.caches[3] = lvl3;
		case 2:

			String[] cache_2_geometry1 = instructions[2].split(",");

			Cache lvl2 = new Cache(Integer.parseInt(cache_2_geometry1[0]),
					Integer.parseInt(cache_2_geometry1[1]),
					Integer.parseInt(cache_2_geometry1[2]),
					Integer.parseInt(cache_2_geometry1[3]),
					cache_2_geometry1[4], cache_2_geometry1[5]);
			simulator.caches[2] = lvl2;
		case 1:
			String[] cache_1_geometry1 = cache_1_geometry.split(",");

			Cache lvl1 = new Cache(Integer.parseInt(cache_1_geometry1[0]),
					Integer.parseInt(cache_1_geometry1[1]),
					Integer.parseInt(cache_1_geometry1[2]),
					Integer.parseInt(cache_1_geometry1[3]),
					cache_1_geometry1[4], cache_1_geometry1[5]);
			simulator.caches[0] = lvl1;
			Cache instructionCache = new Cache(
					Integer.parseInt(cache_1_geometry1[0]),
					Integer.parseInt(cache_1_geometry1[1]),
					Integer.parseInt(cache_1_geometry1[2]),
					Integer.parseInt(cache_1_geometry1[3]),
					cache_1_geometry1[4], cache_1_geometry1[5]);
			simulator.caches[1] = instructionCache;
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
		simulator.R1.setValue(1);
		simulator.R2.setValue(2);
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
		System.out.println("start" + simulator.startAddress);
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
			simulator.currentAddress++;
			simulator.numOfInstructions++;
		}
		simulator.currentAddress = simulator.startAddress;

		simulator.instructionBuffer = new Instruction[simulator.instuctionBufferSize];
		for (int i = 0; i < simulator.instuctionBufferSize; i++)
			simulator.instructionBuffer[i] = new Instruction();

		simulate(simulator);
	}

	public static int randNum(int min, int max) {

		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
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
			current.type = "add";// ??
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
			current.type = "add";
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
		if (instruction.operation.equals("addi")) {
			System.out.println(instruction.Rb.getValue() + instruction.imm
					+ "ss");
			return instruction.Rb.getValue() + instruction.imm;
		}
		if (instruction.operation.equals("sub"))
			return instruction.Rb.getValue() - instruction.Rc.getValue();
		if (instruction.operation.equals("mul"))
			return instruction.Rb.getValue() * instruction.Rc.getValue();
		if (instruction.operation.equals("nand")) {
			// return nand(instruction.Rb.getValue(),
			// instruction.Rc.getValue());
		}
		if (instruction.operation.equals("lw")) {
			return instruction.imm + instruction.Rb.getValue();
		}
		return 0;

	}

	public static void simulate(Simulator s) {
		s.PC.setValue(s.startAddress);
		int value = s.memory_access_time;
		for (int i = 1; i < s.caches.length; i++)
			value += s.caches[i].numCycles;
		System.out.println(s.numOfInstructions);
		boolean fetched = false;
		while (s.PC.getValue() < (s.startAddress + 2 * s.numOfInstructions)
				|| s.ROB_head != s.ROB_tail || s.cycles == s.x) {
			// instructions still exist || or the ROB entry at head is not null
			// continue
			s.cycles++;
			fetched = false;
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
					}

					else if (s.reOrderBuffers[s.ROB_head].instruction.operation.equals("beq")) {
						if (s.false_branch_prediction) {
							if (s.reOrderBuffers[s.ROB_head].instruction.imm >= 0) {
								s.PC.setValue(s.reOrderBuffers[s.ROB_head].instruction.PC_branch
										+ 2
										+ 2
										* s.reOrderBuffers[s.ROB_head].instruction.imm);
							} else {
								s.PC.setValue(s.reOrderBuffers[s.ROB_head].instruction.PC_branch);
							}
							Arrays.fill(s.reOrderBuffers, new ROB());
							for(int i=0;i<s.reservationStations.length;i++){
								s.reservationStations[i].busy = false;
							}
							Arrays.fill(s.instructionBuffer, new Instruction());
							s.isFetching = false;
						} else {
							s.PC.setValue(s.PC.getValue());

						}

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
							for (int j = 0; j < s.reservationStations.length; j++) {
								if (s.reservationStations[j].busy) {
									if (s.reservationStations[j].rj == 0
											&& s.reservationStations[i].type
													.equals(s.reservationStations[j].qj))
										s.reservationStations[j].rj = 1;
									if (s.reservationStations[j].rk == 0
											&& s.reservationStations[i].type
													.equals(s.reservationStations[j].qk))
										s.reservationStations[j].rk = 1;

								}

							}
							s.functionalUnits[i].busy = false;
							s.functionalUnits[i].register = new Register();
							s.reservationStations[i].busy = false;
							s.reOrderBuffers[s.reservationStations[i].destination].isReady = true;
							if (s.reservationStations[i].operation.equals("lw")) {
								s.reOrderBuffers[s.reservationStations[i].destination].value = Integer
										.parseInt(s.mem.memory[computeArithmetic(s.reservationStations[i].instruction)]);
							} else if (s.reservationStations[i].operation
									.equals("sw")) {
								s.reOrderBuffers[s.reservationStations[i].destination].value = s.reservationStations[i].instruction.Ra
										.getValue();
								int swdestination = s.reservationStations[i].instruction.Rb
										.getValue()
										+ s.reservationStations[i].instruction.imm;
								writebackloop: for (int j = 0; j < s.caches.length; j++) {
									for (int k = 0; k < s.caches[j].data.length; k++) {
										if (j != 1) {
											if (s.caches[j].data[k] == (swdestination / s.caches[j].lineSize)
													* s.caches[j].lineSize) {
												if (s.caches[j].writePolicyHit
														.equals("writethrough")) {
													s.mem.memory[swdestination] = ""
															+ s.reservationStations[i].instruction.Ra
																	.getValue();
												} else {
													// wait till tommorow
												}
												break writebackloop;
											}
										}
									}
									s.mem.memory[swdestination] = ""
											+ s.reservationStations[i].instruction.Ra
													.getValue();
									if (s.caches[j].writePolicyMiss
											.equals("writeallocate")) {
										s.addInstructionToCache(swdestination,
												s.caches[j], false);
									}

								}
							} else if (s.reservationStations[i].operation
									.equals("beq")) {
								Instruction current = s.reservationStations[i].instruction;
								int value_to_write;
								boolean imm_positive = s.reservationStations[i].instruction.imm >= 0 ? true
										: false;
								if (imm_positive) {
									// operands equal and immediate is +ve
									// therefore assumption was that branch was
									// not
									// taken. Therefore update value_to_write
									// with destination of branch
									// value_to_write =
									// current.PC_branch+2+(2*current.imm);
									if (s.reservationStations[i].instruction.Ra
											.equals(s.reservationStations[i].instruction.Rb)) {
										s.false_branch_prediction = true;
										
									}
								} else {
									if (!s.reservationStations[i].instruction.Ra
											.equals(s.reservationStations[i].instruction.Rb)) {
										s.false_branch_prediction = true;
									}
								}

							} else {
								s.reOrderBuffers[s.reservationStations[i].destination].value = computeArithmetic(s.reservationStations[i].instruction);
							}

							s.reservationStations[i].busy = false;
							opr = s.reservationStations[i].type;
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
				if (s.instructionBuffer[0] != null) {
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
								if (!s.instructionBuffer[0].operation
										.equals("sw"))
									s.functionalUnits[i].register = s.instructionBuffer[0].Ra;
								else
									s.functionalUnits[i].memoryIndex = s.instructionBuffer[0].Rb
											.getValue()
											+ s.instructionBuffer[0].imm;
								if (s.instructionBuffer[0].type.equals("add")) {
									s.reservationStations[i].remaining_cycles = 2;
								} else if (s.instructionBuffer[0].type
										.equals("mul")) {
									s.reservationStations[i].remaining_cycles = 6;
								} else if (s.instructionBuffer[0].type
										.equals("load")) {
									boolean issuefound = false;
									int destinationAddress = s.instructionBuffer[0].Rb
											.getValue()
											+ s.instructionBuffer[0].imm;
									issueloop: for (int j = 0; j < s.caches.length; j++) {
										s.caches[j].accessed++;
										for (int k = 0; k < s.caches[j].data.length; k++) {
											if (j != 1) {
												if (s.caches[j].data[k] == (destinationAddress / s.caches[j].lineSize)
														* s.caches[j].lineSize) {
													s.reservationStations[i].remaining_cycles += s.caches[j].numCycles;
													issuefound = true;
													s.caches[j].hits++;
													break issueloop;
												}
											}

										}
										if (s.instructionBuffer[0].operation
												.equals("lw"))
											s.addInstructionToCache(
													s.instructionBuffer[0].Rb
															.getValue()
															+ s.instructionBuffer[0].imm,
													s.caches[j], false);
										if (issuefound == false && j != 1) {
											s.reservationStations[i].remaining_cycles += s.caches[j].numCycles;
										}
									}
									if (issuefound == false)
										s.reservationStations[i].remaining_cycles += s.memory_access_time;
								}
								// beq issue new ROB with PC destination and
								// current PC value
								// 10 in memory value here is used to be able in
								// commit stage to identify that this is
								// a beq instruction
								if (s.instructionBuffer[0].operation
										.equals("beq")) {
									s.reOrderBuffers[s.ROB_tail] = new ROB(
											s.instructionBuffer[0].type, s.PC,
											10, s.PC.getValue());
									
									s.reOrderBuffers[s.ROB_tail].instruction = s.instructionBuffer[0];
									System.out.println(s.reOrderBuffers[s.ROB_tail].instruction +"Xxxx");
								} else if (!s.instructionBuffer[0].equals("sw")) {
									s.reOrderBuffers[s.ROB_tail] = new ROB(
											s.instructionBuffer[0].type,
											s.instructionBuffer[0].Ra, -1, 0);
								} else {
									s.reservationStations[i].destination = s.instructionBuffer[0].Rb
											.getValue()
											+ s.instructionBuffer[0].imm;
									s.reOrderBuffers[s.ROB_tail] = new ROB(
											s.instructionBuffer[0].type,
											s.instructionBuffer[0].Ra,
											s.reservationStations[i].destination,
											s.instructionBuffer[0].imm);
								}
								s.reservationStations[i].destination = s.ROB_tail;
								s.ROB_tail++;
								if (s.ROB_tail == s.reOrderBuffers.length)
									s.ROB_tail = 0;
								// get rj rk qj qk
								s.reservationStations[i].rj = 1;
								s.reservationStations[i].rk = 1;
								s.reservationStations[i].qj = "";
								s.reservationStations[i].qk = "";
								if (!s.reservationStations[i].operation
										.equals("beq")) {
									for (int j = 0; j < s.functionalUnits.length; j++) {
										if ((s.functionalUnits[j].register
												.equals(s.instructionBuffer[0].Rb))
												&& j != i) {
											s.reservationStations[i].rj = 0;
											s.reservationStations[i].qj = s.functionalUnits[j].name;
										}

										if ((s.functionalUnits[j].register
												.equals(s.instructionBuffer[0].Rc))
												&& j != i
												&& !s.instructionBuffer[0].type
														.equals("load")) {
											s.reservationStations[i].rk = 0;
											s.reservationStations[i].qk = s.functionalUnits[j].name;
										}
										if ((s.functionalUnits[j].register
												.equals(s.instructionBuffer[0].Ra))
												&& j != i
												&& s.instructionBuffer[0].operation
														.equals("sw")) {
											s.reservationStations[i].rk = 0;
											s.reservationStations[i].qk = s.functionalUnits[j].name;
										}
									}
								} else {
									// set rj rk qj qk corresponding to ra rb
									// operands
									for (int j = 0; j < s.functionalUnits.length; j++) {
										if ((s.functionalUnits[j].register
												.equals(s.instructionBuffer[0].Ra))
												&& j != i) {
											s.reservationStations[i].rj = 0;
											s.reservationStations[i].qj = s.functionalUnits[j].name;
										}
										if ((s.functionalUnits[j].register
												.equals(s.instructionBuffer[0].Rb))
												&& j != i) {
											s.reservationStations[i].rk = 0;
											s.reservationStations[i].qk = s.functionalUnits[j].name;
										}
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
				if (s.PC.getValue() < s.startAddress + 2 * s.numOfInstructions) {
					System.out.println("PC" + s.PC.getValue());
					if (!s.isFetching) {
						s.isFound = false;
						s.fetchCycles = 0;
						cacheLoop: for (int i = 1; i < s.caches.length; i++) {
							s.caches[i].accessed++;
							for (int j = 0; j < s.caches[i].data.length; j++) {
								if (s.caches[i].data[j] == (s.PC.getValue() / s.caches[i].lineSize)
										* s.caches[i].lineSize) {
									s.fetchCycles += s.caches[i].getNumCycles();
									s.caches[i].hits++;
									s.isFound = true;
									break cacheLoop;
								}
							}
							s.addInstructionToCache(s.PC.getValue(),
									s.caches[i], true);
							s.fetchCycles += s.caches[i].getNumCycles();
						}
						if (!s.isFound) {
							s.fetchCycles += s.memory_access_time;
						}
						s.isFetching = true;
					} else {
						s.fetchCycles--;
						if (s.fetchCycles == 0) {
							s.x = (int) s.cycles;
							System.out.println(s.cycles);
							s.instructionBuffer[s.instructionBufferIndex] = decode(
									s, s.mem.memory[s.PC.getValue()]);
							s.instructionBuffer[s.instructionBufferIndex].status = "fetched";
							System.out
									.println(s.instructionBuffer[s.instructionBufferIndex].type);
							if (s.instructionBuffer[s.instructionBufferIndex].operation
									.equals("beq")) {
								if (s.instructionBuffer[s.instructionBufferIndex].imm < 0) {
									s.PC.setValue(s.PC.getValue()
											+ 2
											* (s.instructionBuffer[s.instructionBufferIndex].imm));
								}
								s.instructionBuffer[s.instructionBufferIndex].PC_branch = s.PC
										.getValue();

							}
							if (s.instructionBuffer[s.instructionBufferIndex].operation
									.equals("jmp")) {
								s.PC.setValue(s.instructionBuffer[s.instructionBufferIndex].Ra.getValue()+ s.instructionBuffer[s.instructionBufferIndex].imm);
							}
							s.instructionBufferIndex++;
							System.out
									.println(s.instructionBufferIndex + "ibi");
							s.instructionsInBuffer++;

							s.PC.setValue(s.PC.getValue() + 2);
							s.isFetching = false;
						}
					}

				}
			}

		}

		System.out.println("no. of cycles = " + s.cycles + "");
		System.out.println("IPC = " + s.numOfInstructions / s.cycles);
		for (int i = 0; i < s.caches.length; i++) {
			System.out.println("The hit ratio of cache of L" + i + "="
					+ s.caches[i].hits / s.caches[i].accessed);
		}
		System.out.println("R0 value = " + s.R0.getValue());
		System.out.println("R1 value = " + s.R1.getValue());
		System.out.println("R2 value = " + s.R2.getValue());
		System.out.println("R3 value = " + s.R3.getValue());
		System.out.println("R4 value = " + s.R4.getValue());
		System.out.println("R5 value = " + s.R5.getValue());
		System.out.println(s.mem.memory[3]);
		System.out.println(s.memory_access_time);

	}

	public void addInstructionToCache(int address, Cache c, boolean isInst) {
		boolean found = false;
		int direct_mapped_index;
		int m_set_associative = (address / c.lineSize)
				% (c.lines / c.associativity);
		if (c.type.equals("fully associative")) {
			free: for (int i = 0; i < c.data.length; i++) {
				if (c.data[i] == -1) {
					c.data[i] = (address / c.getLineSize()) * c.getLineSize();
					c.isInstruction[i] = true;
					found = true;
					break free;
				}

			}
			if (!found) {
				int x = randNum(0, c.data.length - 1);
				c.data[x] = (address / c.getLineSize()) * c.getLineSize();
				c.isInstruction[x] = isInst;
			}

		} else if (c.type.equals("direct mapped")) {
			direct_mapped_index = (address / c.lineSize) % c.lines;
			c.data[direct_mapped_index] = (address / c.lineSize) * c.lineSize;
		}
		// else m-set associative
		else {
			for (int i = 0; i < c.associativity; i++) {
				if (c.data[(i * (c.lines / c.associativity))
						+ m_set_associative] == -1) {
					found = true;
					c.data[i] = (address / c.lineSize) * c.lineSize;
				}
			}
			if (!found) {
				int x = randNum(0, c.associativity);
				c.data[(x * (c.lines / c.associativity)) + m_set_associative] = (address * c.lineSize)
						/ c.lineSize;
			}
		}

	}
}
