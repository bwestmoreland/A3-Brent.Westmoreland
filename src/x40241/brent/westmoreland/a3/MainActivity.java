package x40241.brent.westmoreland.a3;

import java.util.ArrayList;
import java.util.List;

import x40241.brent.westmoreland.a3.model.StockInfo;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;



public class MainActivity extends Activity
{
	
	/**
	 * iVars
	 */
	
	private static final String LOGTAG = "MainActivity";
	private CustomListAdapter mListAdapter;
	private ListView mListView;
	private Intent mServiceIntent;
	private BroadcastReceiver mStockDataReceiver;
	private List<StockInfo> mStockList;
	

	/**
	 * Lifecycle
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(getServiceIntent());
		getListView();
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
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(getStockDataReceiver());
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
		if (mListAdapter == null){
			mListAdapter = new CustomListAdapter(this);
		}
		return mListAdapter;
	}

	protected ListView getListView() {
		if (mListView == null){
			mListView = (ListView)findViewById(R.id.list_view);
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
					if(intent.getSerializableExtra(StockServiceImpl.STOCK_SERVICE_INTENT) instanceof ArrayList) {
						@SuppressWarnings("unchecked")
						List<StockInfo> serializableExtra = ((List<StockInfo>)intent.getSerializableExtra(StockServiceImpl.STOCK_SERVICE_INTENT));
						mStockList = serializableExtra;
					}
					getListAdapter().setList(mStockList);
					getListAdapter().notifyDataSetChanged();
				}
			};
		}
		return mStockDataReceiver;
	}


	/**
	 * ListAdapter
	 */
	
	private class CustomListAdapter extends BaseAdapter {
		
        private Context          mContext;
        private List<StockInfo>  mList;
        private LayoutInflater   mLayoutInflater;
        
        CustomListAdapter(Context context) {
            this.mContext = context;
            this.mList = new ArrayList<StockInfo>();
        }
        
        public void setList(List<StockInfo> list){
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
            TextView    priceTextView;
        }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
	            
            if (convertView != null)
                holder = (ViewHolder) convertView.getTag();
            if (holder == null) // not the right view
                convertView = null;
            if (convertView == null) {
                convertView = (LinearLayout) getLayoutInflator().inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.symbolTextView = (TextView) convertView.findViewById(R.id.symbolTextView);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
                holder.priceTextView  = (TextView) convertView.findViewById(R.id.priceTextView);
                convertView.setTag(holder);
            }
            else holder = (ViewHolder) convertView.getTag();
            
            StockInfo stock = mList.get(position);
            holder.symbolTextView.setText(stock.getSymbol());
            holder.nameTextView.setText(stock.getName());
            holder.priceTextView.setText(stock.getPrice() + "");
            return convertView;
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
