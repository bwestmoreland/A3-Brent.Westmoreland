package x40241.brent.westmoreland.a3.model;

public class PriceData {
	
	private long mId;
	private long mStockId;
	private long mTimestamp;
	private float mPrice;

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public long getStockId() {
		return mStockId;
	}

	public void setStockId(long stockId) {
		mStockId = stockId;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(long timestamp) {
		mTimestamp = timestamp;
	}

	public float getPrice() {
		return mPrice;
	}

	public void setPrice(float price) {
		mPrice = price;
	}

}
