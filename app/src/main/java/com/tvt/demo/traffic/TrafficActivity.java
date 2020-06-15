package com.tvt.demo.traffic;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.zdp.aseo.content.AseoZdpAseo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrafficActivity extends Activity
{
	private LinearLayout mCloumLayout;
	private ArrayList<TrafficInfo> infos = new ArrayList<TrafficInfo>();
	private boolean isGeomark = true;
	private Geomark mGeomark;
	private RainAnimotion mRain;
	private TextView mTipTextView;
	private String type = "移动流量";
	private String graph = "折线图";
	private DbManager mDbManager;
	
	private OnClickListener mOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			int id = view.getId();
			switch (id)
				{
				case R.id.traffic_gprs:
					type = "移动流量";
					infos = mDbManager.queryTotal(DbManager.NETWORK_TYPE_MOBILE);
					mGeomark.setData(infos);
					mRain.setData(infos);
					mTipTextView.setText(type + graph);
					if(isGeomark)
					{
						mGeomark.setVisibility(View.GONE);
						mGeomark.setVisibility(View.VISIBLE);
					}
					else {
						mRain.setVisibility(View.GONE);
						mRain.setVisibility(View.VISIBLE);
					}
					break;
				case R.id.traffic_titleBar_back:
					finish();
					break;
				case R.id.traffic_graph_tip_image:
					{
						if(isGeomark)
						{
							isGeomark = false;
							graph = "柱状图";
							mTipTextView.setText(type + graph);
							mGeomark.setVisibility(View.GONE);
							mRain.setVisibility(View.VISIBLE);
						}
						else {
							isGeomark = true;
							graph = "折线图";
							mTipTextView.setText(type + graph);
							mGeomark.setVisibility(View.VISIBLE);
							mRain.setVisibility(View.GONE);
						}
						break;
					}
				case R.id.traffic_wifi:
					type = "wifi数据";
					infos = mDbManager.queryTotal(DbManager.NETWORK_TYPE_WIFI);
					mGeomark.setData(infos);
					mRain.setData(infos);
					mTipTextView.setText(type + graph);
					if(isGeomark)
					{
						mGeomark.setVisibility(View.GONE);
						mGeomark.setVisibility(View.VISIBLE);
					}
					else {
						mRain.setVisibility(View.GONE);
						mRain.setVisibility(View.VISIBLE);
					}
					break;
				default:
					break;
				}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic);
		mCloumLayout = (LinearLayout)findViewById(R.id.traffic_cloum_content);
		 AseoZdpAseo.initType(this,AseoZdpAseo.INSERT_TYPE);
		int width = 0;
		mDbManager= new DbManager(this);
		infos = mDbManager.queryTotal(DbManager.NETWORK_TYPE_WIFI);
		int i;
		long total = 0;
		for(i = 0; i < infos.size(); i++)
		{
			total += infos.get(i).traffic;
		}
		((TextView)findViewById(R.id.traffic_wifi_text)).setText(switchUnit(total));
		width = (infos.size() + 1)*50;
		
		total = 0;
		infos = mDbManager.queryTotal(DbManager.NETWORK_TYPE_MOBILE);
		for(i = 0; i < infos.size(); i++)
		{
			total += infos.get(i).traffic;
		}
		((TextView)findViewById(R.id.traffic_gprs_text)).setText(switchUnit(total));
		if(width < (infos.size() + 1)*50)
			width = (infos.size() + 1)*50;
		
		mGeomark = new Geomark(this);
		mGeomark.setData(infos);
		mRain = new RainAnimotion(this);
		mRain.setData(infos);
		mRain.setVisibility(View.GONE);
		if(width < Utils.getScreenWidth(this))
		{
			mCloumLayout.addView(mGeomark, new LayoutParams(Utils.getScreenWidth(this), LayoutParams.WRAP_CONTENT));
			mCloumLayout.addView(mRain, new LayoutParams(Utils.getScreenWidth(this), LayoutParams.WRAP_CONTENT));
		}
		else
		{
			mCloumLayout.addView(mGeomark, new LayoutParams(width, LayoutParams.WRAP_CONTENT));
			mCloumLayout.addView(mRain, new LayoutParams(width, LayoutParams.WRAP_CONTENT));
		}
		
		findViewById(R.id.traffic_gprs).setOnClickListener(mOnClickListener);
		findViewById(R.id.traffic_titleBar_back).setOnClickListener(mOnClickListener);
		findViewById(R.id.traffic_graph_tip_image).setOnClickListener(mOnClickListener);
		findViewById(R.id.traffic_wifi).setOnClickListener(mOnClickListener);
		mTipTextView = (TextView)findViewById(R.id.traffic_graph_tip_text);
		
	}
	private String switchUnit(long max)
	{
		String textString = "0b";
		if(max/1024 > 0)
		{
			float value = 1f;
			value = 1024;
			String unit = "k";
			if(max/(1024*1024) > 0)
			{
				value = 1024*1024;
				unit = "M";
				if(max/(1024*1024*1024) > 0)
				{
					value = 1024*1024*1024;
					unit = "G";
				}
			}
			float result = max/value;
			BigDecimal b = new BigDecimal(result);// 新建一个BigDecimal
			float displayVar = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();// 进行小数点一位保留处理现实在坐标系上的数值
			textString = displayVar + unit;
		}
		else
		{
			textString = max + "b";
		}
		return textString;
	}
}
