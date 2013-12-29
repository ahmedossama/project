public class reservationStation {
	String name;
	boolean busy;
	String op;
	Register rj;
	Register rk;
	String qj;
	String qk;
	int offset;
	int destination;
	int remaining_cycles;

	public reservationStation(String name) {
		this.name = name;
	}
}
