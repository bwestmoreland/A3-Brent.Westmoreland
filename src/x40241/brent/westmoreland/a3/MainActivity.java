package x40241.brent.westmoreland.a3;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import x40241.brent.westmoreland.a3.StockServiceImpl.LocalBinder;
import x40241.brent.westmoreland.a3.model.StockSummary;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;



public class MainActivity extends Activity
{
	
	/**
	 * iVars
	 */
	
	private static final String LOGTAG = "MainActivity";
	private CustomListAdapter mSummaryListAdapter;
	private ViewSwitcher mViewSwitcher;
	private ListView mListView;
	private Intent mServiceIntent;
	private BroadcastReceiver mStockDataReceiver;
	private List<StockSummary> mStockList;
	private boolean isBound = false;
	private StockServiceImpl mService;
	private TextView mDetailSymbolTextView;
	private TextView mDetailNameTextView;
	private TextView mDetailPriceTextView;
	private TextView mDetailMinTextView;
	private TextView mDetailMaxTextView;
	private TextView mDetailAvgTextView;
	private DecimalFormat mDecimalFormat;
	

	/**
	 * Lifecycle
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(getServiceIntent());
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				StockSummary summary = (StockSummary)getListAdapter().getItem(position);
				getDetailSymbolTextView().setText(summary.getSymbol());
				getDetailNameTextView().setText(summary.getName());
				getDetailMinTextView().setText(summary.getMin() + "");
				getDetailMaxTextView().setText(summary.getMax() +"");
				getDetailAvgTextView().setText(getDecimalFormat().format(summary.getAvg()) + "");
				getDetailPriceTextView().setText(summary.getPrice() +"");
				getViewSwitcher().showNext();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		stopService(getServiceIntent());
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager
			.getInstance(getApplicationContext())
			.registerReceiver(getStockDataReceiver(),new IntentFilter(StockServiceImpl.STOCK_SERVICE_INTENT));
		bindService(getServiceIntent(), mServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(getStockDataReceiver());
		if (isBound) {
			unbindService(mServiceConnection);
			isBound = false;
		}
	}
	
	/**
	 * OnBack Override
	 */
	
	@Override
	public void onBackPressed() {
		if (getViewSwitcher().getNextView() == getListView()){
			getViewSwitcher().showPrevious();
		}
		else {
			super.onBackPressed();
		}
	}
	
	/**
	 * ActionBar
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_search) {
			return search();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean search(){
		Log.d(LOGTAG, "I searched.");
		Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
		//put an extra into the intent if it makes sense
		startActivity(searchIntent);
		return true;
	}
	
	/**
	 * Lazy Getters
	 */
	
	protected CustomListAdapter getListAdapter() {
		if (mSummaryListAdapter == null){
			mSummaryListAdapter = new CustomListAdapter(this);
		}
		return mSummaryListAdapter;
	}

	protected ListView getListView() {
		if (mListView == null){
			mListView = (ListView)findViewById(R.id.summaryListView);
			mListView.setAdapter(getListAdapter());
		}
		return mListView;
	}
	
	protected Intent getServiceIntent() {
		if (mServiceIntent == null) {
			mServiceIntent = new Intent(this, StockServiceImpl.class);
		}
		return mServiceIntent;
	}
	
	protected BroadcastReceiver getStockDataReceiver(){
		if (mStockDataReceiver == null){
			mStockDataReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					Log.d(LOGTAG, "Received message");
					if(intent.getSerializableExtra(StockServiceImpl.STOCK_SERVICE_INTENT) == StockServiceImpl.STOCK_DATA_AVAILABLE) {
						mStockList = mService.getStockSummary();
					}
					getListAdapter().setList(mStockList);
					getListAdapter().notifyDataSetChanged();
				}
			};
		}
		return mStockDataReceiver;
	}
	
	protected ViewSwitcher getViewSwitcher(){
		if (mViewSwitcher == null){
			mViewSwitcher = (ViewSwitcher)findViewById(R.id.viewSwitcher);
			mViewSwitcher.setInAnimation(getInAnimation());
			mViewSwitcher.setOutAnimation(getOutAnimation());
		}
		return mViewSwitcher;
	}
	
	public TextView getDetailSymbolTextView() {
		if (mDetailSymbolTextView == null){
			mDetailSymbolTextView = (TextView)findViewById(R.id.detailSymbolTextView);
		}
		return mDetailSymbolTextView;
	}

	public TextView getDetailNameTextView() {
		if (mDetailNameTextView == null){
			mDetailNameTextView = (TextView)findViewById(R.id.detailNameTextView);
		}
		return mDetailNameTextView;
	}

	public TextView getDetailPriceTextView() {
		if (mDetailPriceTextView == null){
			mDetailPriceTextView = (TextView)findViewById(R.id.detailPriceTextView);
		}
		return mDetailPriceTextView;
	}

	public TextView getDetailMinTextView() {
		if (mDetailMinTextView == null){
			mDetailMinTextView = (TextView)findViewById(R.id.detailMinTextView);
		}
		return mDetailMinTextView;
	}

	public TextView getDetailMaxTextView() {
		if (mDetailMaxTextView == null){
			mDetailMaxTextView = (TextView)findViewById(R.id.detailMaxTextView);
		}
		return mDetailMaxTextView;
	}

	public TextView getDetailAvgTextView() {
		if (mDetailAvgTextView == null){
			mDetailAvgTextView = (TextView)findViewById(R.id.detailAvgTextView);
		}
		return mDetailAvgTextView;
	}
	
	private Animation getInAnimation(){
		return AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left);
	}
	
	private Animation getOutAnimation(){
		return AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right);
	}

	private DecimalFormat getDecimalFormat() {
		if (mDecimalFormat == null){
            mDecimalFormat = new DecimalFormat("##.##");
            mDecimalFormat.setRoundingMode(RoundingMode.DOWN);
		}
		return mDecimalFormat;
	}

	/**
	 * ServiceConnection
	 */
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder)service;
			mService = binder.getService();
			isBound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
			mService = null;
			isBound = false;
		}
	};


	/**
	 * ListAdapter
	 */
	
	private class CustomListAdapter extends BaseAdapter {
		
        private Context          mContext;
        private List<StockSummary>  mList;
        private LayoutInflater   mLayoutInflater;
        private ViewFactory	mPriceViewFactory;
        
        CustomListAdapter(Context context) {
            this.mContext = context;
            this.mList = new ArrayList<StockSummary>();
        }
        
        public void setList(List<StockSummary> list){
        	this.mList = list;
        }

		@Override
		public int getCount() {
            return ((mList == null) ? 0 : mList.size());
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
        class ViewHolder {
            TextView  symbolTextView;
            TextView  nameTextView;
            TextSwitcher priceTextSwitcher;
        }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
            if (convertView != null)
                holder = (ViewHolder) convertView.getTag();
            if (holder == null) // not the right view
                convertView = null;
            if (convertView == null) {
                convertView = (LinearLayout) getLayoutInflator().inflate(R.layout.summary_list_item, null);
                holder = new ViewHolder();
                holder.symbolTextView = (TextView) convertView.findViewById(R.id.symbolTextView);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
                holder.priceTextSwitcher  = (TextSwitcher) convertView.findViewById(R.id.priceTextSwitcher);
                holder.priceTextSwitcher.setFactory(getPriceViewFactory());
                holder.priceTextSwitcher.setInAnimation(getInAnimation());
                holder.priceTextSwitcher.setOutAnimation(getOutAnimation());
                convertView.setTag(holder);
            }
            else holder = (ViewHolder) convertView.getTag();
            
            final StockSummary stock = mList.get(position);
            holder.symbolTextView.setText(stock.getSymbol());
            holder.nameTextView.setText(stock.getName());
            holder.priceTextSwitcher.setText(getDecimalFormat().format(stock.getPrice()) + "");
            return convertView;
		}
		
		private ViewFactory getPriceViewFactory(){
			if (mPriceViewFactory == null) {
				mPriceViewFactory = new ViewFactory() {
					@Override
					public View makeView() {
						TextView priceView = new TextView(MainActivity.this);
						return priceView;
					}
				};	
			}
			return mPriceViewFactory;
		}
        
        private LayoutInflater getLayoutInflator() {
            if (mLayoutInflater == null) {
                mLayoutInflater = (LayoutInflater)
                    this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            return mLayoutInflater;
        }
		
	}
}
