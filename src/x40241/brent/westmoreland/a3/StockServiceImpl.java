package x40241.brent.westmoreland.a3;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import x40241.brent.westmoreland.a3.db.StockDatabaseHelper;
import x40241.brent.westmoreland.a3.db.StockDatabaseHelper.StockCursor;
import x40241.brent.westmoreland.a3.model.PriceData;
import x40241.brent.westmoreland.a3.model.StockInfo;
import x40241.brent.westmoreland.a3.model.StockSummary;
import x40241.brent.westmoreland.a3.net.StockDataSAX;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Skeleton of a basic/simplistic Service with lifcycle stubs enumerated.
 * 
 * @author Jeffrey Peacock (Jeffrey.Peacock@uci.edu)
 */
public class StockServiceImpl
    extends Service
{
    private static final String LOGTAG = "StockServiceImpl";
    private static final boolean DEBUG = true;
    
    public static final String STOCK_SERVICE_INTENT = "x40241.brent.westmoreland.a2.STOCK_SERVICE";
    public static final String STOCK_DATA_AVAILABLE = "NEW DATA";
    public static final String STOCK_SERVICE_URL = "http://wonkware01.appspot.com/stocks.do";
    
    private StockDatabaseHelper mHelper;
    
    /**
     * Public API
     */
    
    public class LocalBinder extends Binder {
        public StockServiceImpl getService() {
            return StockServiceImpl.this;
        }
    }
    
    public static Intent getServiceIntent() {
        Intent intent = new Intent(STOCK_SERVICE_INTENT);
        return intent;
    }
    
    public List<StockSummary> getStockSummary(){
    	List<StockSummary> list = new ArrayList<StockSummary>();
        StockCursor cursor = mHelper.queryStocks();
        if (cursor.moveToFirst())
        {
            do
            {
            	list.add(cursor.getSummary());
            }
            while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed())
        {
            cursor.close();
        }
        return list;
    }
    
    /**
     * Database Support
     */
    
	private void update (final List<StockInfo> list) {

	    for (final StockInfo stockInfo : list) {
	        StockSummary stockSummary = queryBySymbol(stockInfo.getSymbol());  // cache or db operation
	        if (stockSummary == null)
	            stockSummary = add(stockInfo);  // stockInfo now has db id in it.
	        else {
	            final float price = stockInfo.getPrice();
	            stockSummary.setPrice(price);
	            final long count = stockSummary.getCount();
	            stockSummary.setCount(count+1);
	            stockSummary.setMin(Math.min(stockSummary.getMin(), price));
	            stockSummary.setMax(Math.max(stockSummary.getMax(), price));
	            stockSummary.setAvg(((stockSummary.getAvg()*count)+price)/(count+1));
	            stockSummary.setModified(stockInfo.getSequence());  // I have extra field to track modified time
	            if (update(stockSummary) == 1)  // should be 1 for 1 record updated
	            	Log.d(LOGTAG, "update succeeded");
	        }
	        final PriceData priceData = new PriceData();
	        priceData.setStockId(stockSummary.getId());
	        priceData.setTimestamp(stockInfo.getSequence());
	        priceData.setPrice(stockInfo.getPrice());
	        priceData.setId(insert(priceData));  // inserts price data directly to db; sets id for completeness
	    }
	}
	
	private int update(final StockSummary stockSummary){
		return mHelper.updateStockSummary(stockSummary);
	}
	
	private StockSummary add(StockInfo stockInfo){
		return mHelper.insertStockInfo(stockInfo);
	}
	
	private long insert(PriceData priceData){
		return mHelper.insertPriceData(priceData);
	}
	
	private StockSummary queryBySymbol(String stockSymbol){
		StockSummary summary = null;
		StockCursor cursor = mHelper.queryStocks(stockSymbol);
        if (cursor.moveToFirst())
        {
            do
            {
            	summary = cursor.getSummary();
            }
            while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed())
        {
            cursor.close();
        }
		return summary;
	}
    
    /**
     * Notifications
     */
    
    private int NOTIFICATION = R.string.stock_service_started;
    private NotificationManager mNM;
    
    @SuppressWarnings("deprecation")
	private void showNotification() {
        CharSequence text = getText(R.string.stock_service_started);
        
		Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        
        notification.setLatestEventInfo(this, getText(R.string.stock_service_label),
                       text, contentIntent);

        mNM.notify(NOTIFICATION, notification);
    }
   

    /**
     * Timer
     */
    
    private Timer mStockTimer;
    
    class StockListTimerTask extends TimerTask {
		@Override
		public void run() {
		    List<StockInfo> list = getStockData();
		    update(list);
			Log.d("sender", "Broadcasting message");
			Intent intent = new Intent(STOCK_SERVICE_INTENT);
			intent.putExtra(STOCK_SERVICE_INTENT, STOCK_DATA_AVAILABLE);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
		}
    }
    
    private StockListTimerTask mStockTimerTask;
    
    // Track if we've been started at least once.
    private boolean isInitialized = false;
    
    // Track if a client Activity is bound to us.
    private boolean isBound = false;
    
    // This is the object that receives interactions from clients.
    private final IBinder localBinder = new LocalBinder();
    
      
    /**
     * Setup and Teardown
     */
    
    private void initialize(){
    	mHelper = new StockDatabaseHelper(getApplicationContext());
    	mStockTimerTask = new StockListTimerTask();
    	mStockTimer = new Timer();
    	mStockTimer.schedule(mStockTimerTask, 1000, 5000);
    	isInitialized = true;
    }
    
    private void teardown(){
    	mStockTimer.cancel();
    	mStockTimer = null;
    	mStockTimerTask = null;
    	mHelper = null;
    	isInitialized = false;
    }
    
    //**********************************************************************************************
    //  LIFECYCLE METHODS
    //**********************************************************************************************
    @Override
    public void onCreate() {
        Log.d (LOGTAG, "*** onCreate(): STARTING");
        Log.d (LOGTAG, "*** onCreate(): ENDING");
        
        // Display a notification about us starting. We put an icon in the status bar.
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
    }
    
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        if (DEBUG) {
            Log.d (LOGTAG, "*** onStartCommand(): STARTING; initialized="+isInitialized);
            Log.d (LOGTAG, "*** onStartCommand(): flags="+flags);
            Log.d (LOGTAG, "*** onStartCommand(): intent="+intent);
        }
        if (isInitialized)
            return START_STICKY;
        initialize();
        
        Log.d (LOGTAG, "*** onStart(): ENDING");
        // We want this service to continue running until it is explicitly stopped.
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.d (LOGTAG, "*** onDestroy()");
        teardown();
        mNM.cancel(NOTIFICATION);
    }
    
    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        Log.d (LOGTAG, "*** onConfigurationChanged()");
    }
    
    @Override
    public void onLowMemory() {
        Log.d (LOGTAG, "*** onLowMemory()");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d (LOGTAG, "*** onBind()");
        if (DEBUG) {
            Log.d (LOGTAG, "*** onBind(): action="+intent.getAction());
            Log.d (LOGTAG, "*** onBind(): toString="+intent.toString());
        }
        isBound = true;
        return localBinder;
    }

    @Override
    public boolean onUnbind (Intent intent) {
        Log.d (LOGTAG, "*** onUnbind()");
        if (DEBUG) {
            Log.d (LOGTAG, "*** onUnbind(): action="+intent.getAction());
            Log.d (LOGTAG, "*** onUnbind(): toString="+intent.toString());
        }
        isBound = false;
        return true;
    }
    
    @Override
    public void onRebind (Intent intent) {
        Log.d (LOGTAG, "*** onRebind()");
        if (DEBUG) {
            Log.d (LOGTAG, "*** onUnbind(): action="+intent.getAction());
            Log.d (LOGTAG, "*** onUnbind(): toString="+intent.toString());
        }
    }
    
    //  utility method for retrieving stock data.
    private List<StockInfo> getStockData()
    {
        URL url = null;
        InputStream in = null;
        List<StockInfo> stockData = null;
        try {
            url = new URL(STOCK_SERVICE_URL);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {                
            	return getDummyData();
            }
            in = httpConnection.getInputStream();
            stockData = new StockDataSAX().parse(in);
        }
        catch (UnknownHostException e) {
        	e.printStackTrace();
        	return getDummyData();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return getDummyData();
        }
        catch (IOException e) {
            e.printStackTrace();
            return getDummyData();
        }
        catch (Throwable t) {
            //  At least ensure the thread always ends orderly, even 
            //  in the event of something completely unexpected
            t.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                }
                catch (Exception e) { /* ignore */ }
            }
        }
        Log.d(LOGTAG, stockData.toString());
        return stockData;
    }
    
    private List<StockInfo> getDummyData(){
    	//TODO: This is a temporary hack to handle bad data from the network,
    	//I'm aware that I need to add better network handling but don't have the time to look it up.
        List<StockInfo> dummyList = new ArrayList<StockInfo>();
        StockInfo networkErrorStockInfo = new StockInfo();
        networkErrorStockInfo.setSymbol("Please ");
        networkErrorStockInfo.setName("check your Internet connection");
        dummyList.add(networkErrorStockInfo);
        return dummyList;
    }

	protected boolean isBound() {
		return isBound;
	}
}
