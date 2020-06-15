package com.tvt.demo.traffic;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbManager
{
	public static final String NETWORK_TYPE_MOBILE = "Gprs";
	public static final String NETWORK_TYPE_WIFI = "Wifi";
	public static final String DB_NAME = "traffic.db";
	public static final int DB_VERSION = 1;
	public final String COLUMN_ID = android.provider.BaseColumns._ID;
	private DbHelper dbHelper;
	private SQLiteDatabase wDb;
	private SQLiteDatabase rDb;

	public DbManager(Context context)
	{
		dbHelper = new DbHelper(context);
		wDb = dbHelper.getWritableDatabase();
		rDb = dbHelper.getReadableDatabase();
	}

	public void close()
	{
		wDb.close();
		rDb.close();
		dbHelper.close();
	}

	public void insertStart(TrafficInfo info) 
	{
		ContentValues value = new ContentValues();
		value.put(DbHelper.COLUMN_DATE, info.date);
		value.put(DbHelper.COLUMN_TYPE, info.type);
		value.put(DbHelper.COLUMN_TRAFFIC, info.traffic);
		wDb.insert(DbHelper.TABLE_NAME, null, value);
	}

	public void updateTraffic(long traffic, String date, String type) 
	{

		Cursor cursor = rDb.rawQuery("select * from " + DbHelper.TABLE_NAME + " where Date = ? and Type = ? ",new String[] { date,type });
		if(cursor.moveToNext())
		{
			long lastTraffic ;
			ContentValues value = new ContentValues();
			lastTraffic = cursor.getLong(cursor.getColumnIndex(DbHelper.COLUMN_TRAFFIC));
			value.put(DbHelper.COLUMN_TRAFFIC,lastTraffic + traffic);
			wDb.update(DbHelper.TABLE_NAME,value,"_id"+ "=" + cursor.getInt(cursor.getColumnIndex("_id")),null);
		}
		else 
		{
			TrafficInfo info = new TrafficInfo();
			info.date = date;
			info.type = type;
			info.traffic = traffic;
			insertStart(info);
		}
		cursor.close();
	}

	public ArrayList<TrafficInfo> queryTotal(String type)
	{
		Cursor cursor = rDb.rawQuery("select * from " + DbHelper.TABLE_NAME + " where Type = ? ",new String[] { type });//rDb.query(DbHelper.TABLE_NAME, null, null, null, null, null,null);
		ArrayList<TrafficInfo> trafficInfos = new ArrayList<TrafficInfo>();
		TrafficInfo temp = new TrafficInfo();
		temp.date = "00";
		temp.type = "";
		temp.traffic = 0;
		trafficInfos.add(temp);
		while (cursor.moveToNext())
		{
			TrafficInfo item = new TrafficInfo();
			item.date = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_DATE));
			item.type = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TYPE));
			item.traffic = cursor.getLong(cursor.getColumnIndex(DbHelper.COLUMN_TRAFFIC));
			if(item.traffic < 0)
				item.traffic = 0 - item.traffic;
			trafficInfos.add(item);
		}
		cursor.close();
		return trafficInfos;
	}
}
