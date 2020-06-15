package com.tvt.demo.traffic;

import java.util.List;

import android.R.integer;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/*
 * 问题点：
 * 1.如果服务被强制关闭，则不能统计
 * 2.开机自动启动服务
 * 3.如果要做某天的流量统计，则需要定时在每晚十二点前进行一次已用流量统计
 * 4.数据库相关操作比较耗时，要考虑异步
 * 
 */
public class TrafficService extends Service
{
	private static final String TAG = TrafficService.class.getSimpleName();
	private static final String SERVICE_SP = "service_sp";
	private static final String SERVICE_LAST_TRAFFIC = "service_last_traffic";

	private final int REFRESH_MESSAGE = 1;
	
//	private TrafficReceiver tReceiver;
	private ConnectivityManager connManager;
	private DbManager dbManager;

	private MyBinder binder = new MyBinder();

	private Handler refreshHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			logRecord();
			Log.e("#################", "record");
			sendEmptyMessageDelayed(REFRESH_MESSAGE, 60000);
		}
		
	};
	
	public class MyBinder extends Binder 
	{
		TrafficService getService()
		{
			return TrafficService.this;
		}
	}

	public IBinder onBind(Intent intent) 
	{
		Log.d(TAG, "onBind");
		return binder;
	}

	public void onCreate()
	{
		Log.d(TAG, "onCreate");
		// 获得数据库连接服务
		dbManager = new DbManager(this);
		// 获得网络连接服务
		connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 注册TrafficReceiver
//		tReceiver = new TrafficReceiver();
//		IntentFilter filter = new IntentFilter();
//		// filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//		registerReceiver(tReceiver, filter);
		refreshHandler.sendEmptyMessageDelayed(REFRESH_MESSAGE,60000);
		super.onCreate();
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy()
	{
		Log.d(TAG, "onDestroy");
//		unregisterReceiver(tReceiver);
		logRecord();
		dbManager.close();
		refreshHandler.removeMessages(REFRESH_MESSAGE);
		super.onDestroy();
	}

	public synchronized  void logRecord()
	{
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		String networkType = null;
		if (networkInfo == null)
		{
			networkType = null;
		} 
		else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			networkType = DbManager.NETWORK_TYPE_WIFI;
		} 
		else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			networkType = DbManager.NETWORK_TYPE_MOBILE;
		}

		long ret = (long) 0.0;
		long lastTraffic = (Long)Utils.get(getApplicationContext(), networkType+SERVICE_SP, SERVICE_LAST_TRAFFIC, ret);
		int uid = getApplicationInfo().uid;;
		long rx = TrafficStats.getUidRxBytes(uid);
		long tx = TrafficStats.getUidTxBytes(uid);
		long traffic = rx + tx;
		Utils.put(getApplicationContext(), SERVICE_SP,networkType + SERVICE_LAST_TRAFFIC, traffic);
		
//		String date = Utils.getCurrentTime("MM-dd");
		String date = "04-" + System.currentTimeMillis()%30;
		if(traffic < lastTraffic)
			dbManager.updateTraffic(traffic, date, networkType);
		else 
			dbManager.updateTraffic(traffic - lastTraffic, date, networkType);			
	}
}
