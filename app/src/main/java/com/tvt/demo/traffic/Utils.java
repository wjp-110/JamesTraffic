package com.tvt.demo.traffic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class Utils
{
	public Utils()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}
	
	/**获取当前时间
	 * format:yyyy-MM-dd HH:mm:ss
	 * */
	public static String getCurrentTime(String format) 
	{
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(new Date());
	}
	
	/**
	 * 获得屏幕高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context)
	{
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	/**
	 * 保存数据的方法，我们要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
	 * 
	 * @param context
	 * @param key
	 * @param object
	 */
	public static void put(Context context, String spname, String key, Object object)
	{

		SharedPreferences sp = context.getSharedPreferences(spname,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();

		if (object instanceof String)
		{
			editor.putString(key, (String) object);
		} 
		else if (object instanceof Integer)
		{
			editor.putInt(key, (Integer) object);
		} 
		else if (object instanceof Boolean)
		{
			editor.putBoolean(key, (Boolean) object);
		} 
		else if (object instanceof Float)
		{
			editor.putFloat(key, (Float) object);
		}
		else if (object instanceof Long)
		{
			editor.putLong(key, (Long) object);
		} 
		else
		{
			editor.putString(key, object.toString());
		}

		SharedPreferencesCompat.apply(editor);
	}

	/**
	 * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取?
	 * 
	 * @param context
	 * @param key
	 * @param defaultObject
	 * @return
	 */
	public static Object get(Context context, String spname, String key, Object defaultObject)
	{
		SharedPreferences sp = context.getSharedPreferences(spname,
				Context.MODE_PRIVATE);

		if (defaultObject instanceof String)
		{
			return sp.getString(key, (String) defaultObject);
		} 
		else if (defaultObject instanceof Integer)
		{
			return sp.getInt(key, (Integer) defaultObject);
		} 
		else if (defaultObject instanceof Boolean)
		{
			return sp.getBoolean(key, (Boolean) defaultObject);
		} 
		else if (defaultObject instanceof Float)
		{
			return sp.getFloat(key, (Float) defaultObject);
		} 
		else if (defaultObject instanceof Long)
		{
			return sp.getLong(key, (Long) defaultObject);
		}

		return null;
	}
	
	/**
	 * 创建解决SharedPreferencesCompat.apply方法的一个兼容类
	 *
	 * 
	 */
	private static class SharedPreferencesCompat
	{
		private static final Method sApplyMethod = findApplyMethod();

		/**
		 * 反射查找apply的方法?
		 * 
		 * @return
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static Method findApplyMethod()
		{
			try
			{
				Class clz = SharedPreferences.Editor.class;
				return clz.getMethod("apply");
			} 
			catch (NoSuchMethodException e){
			}

			return null;
		}

		/**
		 * 如果找到则使用apply执行，否则使用commit
		 * 
		 * @param editor
		 */
		public static void apply(SharedPreferences.Editor editor)
		{
			try
			{
				if (sApplyMethod != null)
				{
					sApplyMethod.invoke(editor);
					return;
				}
			} 
			catch (IllegalArgumentException e){
			} 
			catch (IllegalAccessException e){
			} 
			catch (InvocationTargetException e){
			}
			editor.commit();
		}
	}
	
}
