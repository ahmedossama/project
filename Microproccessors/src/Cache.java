public class Cache {
	private int size; //
	private int lineSize;
	private int associativity;
	private String writePolicyHit;
	private String writePolicyMiss;
	private int lines;
	private String type;
	private int numCycles;
	private CacheEntry[] cacheEntries;

	public Cache(int s, int l, int m, int numCycles, String writePolicyHit,
			String writePolicyMiss) {
		setSize(s);
		setLineSize(l);
		setAssociativity(m);
		this.setWritePolicyHit(writePolicyHit);
		this.setWritePolicyMiss(writePolicyMiss);
		setLines(getSize() / getLineSize());
		this.setNumCycles(numCycles);
		if (getAssociativity() == getLines()) {
			setType("fully associative");
		} else if (getAssociativity() == 1) {
			setType("direct mapped");
		} else {
			setType("set associative");
		}
		this.setCacheEntries(new CacheEntry[getLines()]);

	}

	public String getWritePolicyHit() {
		return writePolicyHit;
	}

	public String getWritePolicyMiss() {
		return writePolicyMiss;
	}

	public void setWritePolicyHit(String writePolicyHit) {
		this.writePolicyHit = writePolicyHit;
	}

	public void setWritePolicyMiss(String writePolicyMiss) {
		this.writePolicyMiss = writePolicyMiss;
	}

	public int getAssociativity() {
		return associativity;
	}

	public void setAssociativity(int associativity) {
		this.associativity = associativity;
	}

	public int getLineSize() {
		return lineSize;
	}

	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getLines() {
		return lines;
	}

	public void setLines(int lines) {
		this.lines = lines;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getNumCycles() {
		return numCycles;
	}

	public void setNumCycles(int numCycles) {
		this.numCycles = numCycles;
	}

	public void setCacheEntries(CacheEntry[] cacheEntries) {
		this.cacheEntries = cacheEntries;
	}

	public CacheEntry[] getCacheEntries() {
		return cacheEntries;
	}
}
