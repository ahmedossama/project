public class reservationStation {
	String name;
	boolean busy;
	String operation;
	Register rj;
	Register rk;
	String qj;
	String qk;
	int offset;
	int destination;
	int remaining_cycles;
	Register rDestination;
	public reservationStation(String name) {
		this.name = name;
	}
}
