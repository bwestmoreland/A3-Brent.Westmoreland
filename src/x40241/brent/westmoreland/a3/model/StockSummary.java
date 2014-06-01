package x40241.brent.westmoreland.a3.model;


/**
 * @author Jeffrey Peacock (Jeffrey.Peacock@uci.edu)
 */
public final class StockSummary
{
	private long mId;
	private long   mSequence;
    private String  mName;
    private String  mSymbol;
    private Float  mPrice;
    private Float mMax;
    private Float mMin;
    private Float mAvg;
    private long mCount;
    private long mModified;
    
    public long getId() {
		return mId;
	}
    
	public void setId(long stockID) {
		mId = stockID;
	}
	
    public long getSequence() {
        return mSequence;
    }
    public void setSequence (long sequence) {
        this.mSequence = sequence;
    }
    public String getName() {
        return mName;
    }
    public void setName (String name) {
        this.mName = name;
    }
    public String getSymbol() {
        return mSymbol;
    }
    public void setSymbol (String symbol) {
        this.mSymbol = symbol;
    }
    public Float getPrice() {
        return mPrice;
    }
    public void setPrice (Float price) {
        this.mPrice = price;
    }

	public Float getMax() {
		return mMax;
	}
	
	public void setMax(Float max) {
		mMax = max;
	}

	public Float getMin() {
		return mMin;
	}

	public void setMin(Float min) {
		mMin = min;
	}

	public Float getAvg() {
		return mAvg;
	}

	public void setAvg(Float avg) {
		mAvg = avg;
	}

	public long getCount() {
		return mCount;
	}

	public void setCount(long count) {
		mCount = count;
	}

	public long getModified() {
		return mModified;
	}

	public void setModified(long modified) {
		mModified = modified;
	}
}


