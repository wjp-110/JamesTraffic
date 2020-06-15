package com.tvt.demo.traffic;

import com.zdp.aseo.content.AseoZdpAseo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity
{
	private TrafficService trafficService;
	
	private ServiceConnection mConnection = new ServiceConnection()
	{

		@Override
		public void onServiceDisconnected(ComponentName name) {
			trafficService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			trafficService = ((TrafficService.MyBinder) service).getService();
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AseoZdpAseo.init(this,AseoZdpAseo.SCREEN_TYPE);
        findViewById(R.id.cta).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(MainActivity.this, TrafficActivity.class);
				startActivity(intent);
			}
		});
        
		AseoZdpAseo.initFinalTimer(this, AseoZdpAseo.BOTH_TYPE);
		Intent intent = new Intent(MainActivity.this, TrafficService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

	@Override
	public void onBackPressed() 
	{
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy()
	{
		unbindService(mConnection);
		super.onDestroy();
	}
}
