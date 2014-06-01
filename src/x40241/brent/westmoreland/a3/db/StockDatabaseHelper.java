package x40241.brent.westmoreland.a3.db;

import x40241.brent.westmoreland.a3.model.PriceData;
import x40241.brent.westmoreland.a3.model.StockInfo;
import x40241.brent.westmoreland.a3.model.StockSummary;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StockDatabaseHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "stocks.sqlite";
	private static final int VERSION = 5;
	
	private static final String TABLE_STOCK = "stock_summary";
	private static final String COLUMN_STOCK_ID = "_id";
	private static final String COLUMN_STOCK_SYMBOL = "symbol";
	private static final String COLUMN_STOCK_NAME = "name";
	private static final String COLUMN_STOCK_MIN = "min";
	private static final String COLUMN_STOCK_MAX = "max";
	private static final String COLUMN_STOCK_AVG= "avg";
	private static final String COLUMN_STOCK_COUNT = "count";

	private static final String TABLE_PRICE = "price_info";
	private static final String COLUMN_PRICE_ID = "_id";
	private static final String COLUMN_PRICE_SEQUENCE = "sequence";
	private static final String COLUMN_PRICE_STOCK_ID = "stock_id";
	private static final String COLUMN_PRICE_PRICE = "price";

	public StockDatabaseHelper(Context context) 
	{
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the "stock_info" table
        db.execSQL("CREATE TABLE " + TABLE_STOCK + " ("
        	+ COLUMN_STOCK_ID + " INTEGER PRIMARY KEY, "
        	+ COLUMN_STOCK_SYMBOL + " TEXT UNIQUE, " 
        	+ COLUMN_STOCK_NAME + " TEXT, "
        	+ COLUMN_STOCK_MIN + " REAL, "
        	+ COLUMN_STOCK_MAX + " REAL, "
        	+ COLUMN_STOCK_AVG + " REAL, "
        	+ COLUMN_STOCK_COUNT + " INTEGER"
        	+");");
        	
        // Create the "price_info" table
        db.execSQL("CREATE TABLE " + TABLE_PRICE + " (" 
        	+ COLUMN_PRICE_ID + " INTEGER PRIMARY KEY, "
        	+ COLUMN_PRICE_PRICE + " REAL, "
        	+ COLUMN_PRICE_SEQUENCE + " INTEGER, "
        	+ COLUMN_PRICE_STOCK_ID + " INTEGER, FOREIGN KEY(" + COLUMN_PRICE_STOCK_ID + ") REFERENCES " + TABLE_STOCK + "(" + COLUMN_STOCK_ID + ")" 
        	+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE);
		onCreate(db);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);

	}
	
	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);
	    if (!db.isReadOnly()) {
	        // Enable foreign key constraints
//	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}
	
	public long insertPriceData(PriceData priceData){
		ContentValues priceValues = new ContentValues();
		priceValues.put(COLUMN_PRICE_PRICE, priceData.getPrice());
		priceValues.put(COLUMN_PRICE_SEQUENCE, priceData.getTimestamp());
		priceValues.put(COLUMN_PRICE_STOCK_ID, priceData.getStockId());
		return getWritableDatabase().insert(TABLE_PRICE, null, priceValues);
	}
	
	public StockSummary insertStockInfo(StockInfo stockInfo) {
		ContentValues stockValues = new ContentValues();
		stockValues.put(COLUMN_STOCK_SYMBOL, stockInfo.getSymbol());
		stockValues.put(COLUMN_STOCK_NAME, stockInfo.getName());
		long stockId = getWritableDatabase().insertWithOnConflict(TABLE_STOCK, null, stockValues, SQLiteDatabase.CONFLICT_REPLACE);
		StockSummary summary = new StockSummary();
		summary.setId(stockId);
		summary.setName(stockInfo.getName());
		summary.setSymbol(stockInfo.getSymbol());
		return summary;
	}
	
	public int updateStockSummary(StockSummary stockSummary) {
		ContentValues stockValues = new ContentValues();
		stockValues.put(COLUMN_STOCK_ID, stockSummary.getId());
		stockValues.put(COLUMN_STOCK_SYMBOL, stockSummary.getSymbol());
		stockValues.put(COLUMN_STOCK_NAME, stockSummary.getName());
		stockValues.put(COLUMN_STOCK_MIN, stockSummary.getMin());
		stockValues.put(COLUMN_STOCK_MAX, stockSummary.getMax());
		stockValues.put(COLUMN_STOCK_AVG, stockSummary.getAvg());
		stockValues.put(COLUMN_STOCK_COUNT, stockSummary.getCount());
		long stockId = getWritableDatabase().insertWithOnConflict(TABLE_STOCK, null, stockValues, SQLiteDatabase.CONFLICT_REPLACE);
		if (stockId >= 0){
			return 1;
		}
		return -1;
	}
	
	public StockCursor queryStocks(){
		Cursor wrapped = getReadableDatabase().query(
				TABLE_STOCK,  	//Table
				null, 			//Columns
				null, 			//Selection
				null, 			//Selection Args
				null, 			//Group by
				null,			//having 
				COLUMN_STOCK_SYMBOL			//order by
			);
		return new StockCursor(wrapped);
	}
	
	public StockCursor queryStocks(String stockSymbol){
		Cursor wrapped = getReadableDatabase().query(
				TABLE_STOCK,  	//Table
				null, 			//Columns
				COLUMN_STOCK_SYMBOL + " = ?", 			//Selection
				new String[] { stockSymbol }, 			//Selection Args
				null, 			//Group by
				null,			//having 
				null			//order by
			);
		return new StockCursor(wrapped);
	}
	
	
	
	public static class StockCursor extends CursorWrapper {
		
		public StockCursor(Cursor cursor){
			super(cursor);
		}
		
		public StockSummary getSummary(){
			if (isBeforeFirst() || isAfterLast()){
				return null;
			}
			StockSummary summary = new StockSummary();
			summary.setId( getLong(getColumnIndex(COLUMN_STOCK_ID)));
			summary.setMax(getFloat(getColumnIndex(COLUMN_STOCK_MAX)));
			summary.setMin(getFloat(getColumnIndex(COLUMN_STOCK_MIN)));
			summary.setAvg(getFloat(getColumnIndex(COLUMN_STOCK_AVG)));
			summary.setCount(getLong(getColumnIndex(COLUMN_STOCK_COUNT)));
			summary.setName(getString(getColumnIndex(COLUMN_STOCK_NAME)));
			summary.setSymbol(getString(getColumnIndex(COLUMN_STOCK_SYMBOL)));
//			summary.setModified(getLong(getColumnIndex(COLUMN_STOCK_MODIFIED)));
			return summary;
		}
	}
	
}
