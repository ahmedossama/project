public class CacheEntry {

	private boolean isValid;
	private String tag;
	private String data;
	private String offSet;
	private String key;

	public CacheEntry() {

	}

	public CacheEntry(String key, boolean isValid, String tag, String data,
			String offSet) {

		this.isValid = isValid;
		this.tag = tag;
		this.data = data;
		this.offSet = offSet;
		this.key = key;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setOffSet(String offSet) {
		this.offSet = offSet;
	}
	public void setKey(String key){
		this.key = key;
	}

}
