package x40241.brent.westmoreland.a3.model;


/**
 * @author Jeffrey Peacock (Jeffrey.Peacock@uci.edu)
 */
public final class StockInfo
{
    private long   sequence;
    private String  name;
    private String  symbol;
    private String  price;
    
    public long getSequence() {
        return sequence;
    }
    public void setSequence (long sequence) {
        this.sequence = sequence;
    }
    public String getName() {
        return name;
    }
    public void setName (String name) {
        this.name = name;
    }
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol (String symbol) {
        this.symbol = symbol;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice (String price) {
        this.price = price;
    }
}


