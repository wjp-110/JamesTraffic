package com.tvt.demo.traffic;

import java.math.BigDecimal;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Geomark extends SurfaceView implements SurfaceHolder.Callback 
{
	private int currentX;
	private int oldX;
	private SurfaceHolder sfh;
	private boolean isRunning = true;

	private int tick = 20; // 时间间隔(ms)
	private int bottom = 150; // 坐标系地段距离框架顶端的距离
	private int top = 30; // 坐标系顶端距离框架顶端框的距离
	private int lift = 38; // 坐标系左边距离框架左边框的距离
	static int right  =30; // 坐标系右边距离框架左边的距离(!)
	static int gapX = 50; // 两根竖线间的间隙(!)
	private int gapY = 20; // 两根横线间的间隙
	
	private float unit = 1f;
	private String unitString = "b";
	
	private ArrayList<TrafficInfo> mData = new ArrayList<TrafficInfo>();

	public Geomark(Context context)
	{
		super(context);
		init(context);
	}

	// 在这里初始化才是最初始化的。
	public Geomark(Context context, AttributeSet atr) {
		super(context, atr);
		init(context);
	}
	
	private void init(Context context)
	{
		setZOrderOnTop(true);// 设置置顶（不然实现不了透明）
		sfh = this.getHolder();
		sfh.addCallback(this);
		sfh.setFormat(PixelFormat.TRANSLUCENT);// 设置背景透明
		
		bottom = dip2px(context, 150);
		gapY = (bottom - top ) / 7;
	}
	
	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	
	public void setData(ArrayList<TrafficInfo> data)
	{
		mData = data;
	}

	/**
	 * @see SurfaceHolder.Callback#surfaceCreated(SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("系统消息", "surfaceCreated");

		// 加入下面这三句是当抽屉隐藏后，打开时防止已经绘过图的区域闪烁，所以干脆就从新开始绘制。
		isRunning = true;
		currentX = 0;
		clearCanvas();

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				gridDraw();
				drawChartLine();
			}
		});

		thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.i("系统信息", "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.i("系统信息", "surfaceDestroyed");

		// 加入这个变量是为了控制抽屉隐藏时不会出现异常。
		isRunning = false;
	}

	protected void gridDraw() 
	{
		long max = mData.get(0).traffic;
		long temMax = max;
		long min = max;
		long temMin = max;
		float space = 0f;// 平均值
		for (int i = 1; i < mData.size(); i++)
		{
			if (max <  mData.get(i).traffic)
			{
				max = mData.get(i).traffic;
			}
			if (min > mData.get(i).traffic) 
			{
				min = mData.get(i).traffic;
			}
			temMax = max;
			temMin = min;
		}
		
		switchUnit(temMax);
//		temMax = (long) (temMax/unit);
//		temMin = (long) (temMin/unit);
		space = (temMax - temMin) / (unit*7.0f);

		
		Canvas canvas = sfh.lockCanvas();
		Paint mbackLinePaint = new Paint();// 用来画坐标系了
		mbackLinePaint.setColor(Color.GRAY);
		mbackLinePaint.setAntiAlias(true);
		mbackLinePaint.setStrokeWidth(1);
		mbackLinePaint.setStyle(Style.FILL);

		Paint mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(12F);// 设置温度值的字体大小
		
		Paint mPointPaint = new Paint();
		mPointPaint.setAntiAlias(true);
		mPointPaint.setColor(Color.DKGRAY);
		
		// 绘制坐标系
		for (int i = 0; i < 8; i++) 
		{
			if(i == 7)
				canvas.drawLine(lift, bottom, lift + gapX * mData.size(),bottom, mbackLinePaint);
			//	canvas.drawLine(lift, top + gapY * i, lift + gapX * mData.size(), top + gapY * i, mbackLinePaint);
			else
				canvas.drawCircle(lift, top + gapY * i + 4, 2, mPointPaint);
			mTextPaint.setTextAlign(Align.RIGHT);
			float result = temMin + space * i;// 精确的各个节点的值
			BigDecimal b = new BigDecimal(result);// 新建一个BigDecimal
			float displayVar = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();// 进行小数点一位保留处理现实在坐标系上的数值
			canvas.drawText("" + displayVar, lift - 2, bottom + 3 - gapY * i, mTextPaint);
		}
		for (int i = 0; i < mData.size(); i++) 
		{
			if(i == 0)
				canvas.drawLine(lift + gapX * i, top, lift + gapX * i, bottom,mbackLinePaint);
			mTextPaint.setTextAlign(Align.CENTER);
			canvas.drawText(mData.get(i).date, lift + gapX * i, bottom + 14, mTextPaint);
			canvas.drawCircle(lift + gapX * i, bottom , 2, mPointPaint);
		}
		sfh.unlockCanvasAndPost(canvas);
	}

	protected void GridDraw(Canvas canvas) 
	{
		if (canvas == null) 
		{
			return;
		}
		long max = mData.get(0).traffic;
		long temMax = max;
		long min = max;
		long temMin = max;
		float space = 0;// 平均值
		for (int i = 1; i < mData.size(); i++) 
		{
			if (max < mData.get(i).traffic) {
				max = mData.get(i).traffic;
			}
			if (min > mData.get(i).traffic) {
				min = mData.get(i).traffic;
			}
			temMax = max;
			temMin = min;
		}
		switchUnit(temMax);
//		temMax = (long) (temMax/unit);
//		temMin = (long) (temMin/unit);
		space = (temMax - temMin) / (unit*7.0f);

		Paint mbackLinePaint = new Paint();// 用来画坐标系了
		mbackLinePaint.setColor(Color.GRAY);
		mbackLinePaint.setAntiAlias(true);
		mbackLinePaint.setStrokeWidth(1);
		mbackLinePaint.setStyle(Style.FILL);

		Paint mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		// mTextPaint.setTextAlign(Align.RIGHT);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(12F);// 设置温度值的字体大小
		
		Paint mPointPaint = new Paint();
		mPointPaint.setAntiAlias(true);
		mPointPaint.setColor(Color.DKGRAY);
		// 绘制坐标系
		for (int i = 0; i < 8; i++) 
		{
			if(i == 7)
				canvas.drawLine(lift, bottom, lift + gapX * mData.size(),bottom, mbackLinePaint);
			//	canvas.drawLine(lift, top + gapY * i, lift + gapX * mData.size(), top + gapY * i, mbackLinePaint);
			else
				canvas.drawCircle(lift, top + gapY * i + 4, 2, mPointPaint);
			mTextPaint.setTextAlign(Align.RIGHT);
			float result = temMin + space * i;// 精确的各个节点的值
			BigDecimal b = new BigDecimal(result);// 新建一个BigDecimal
			float displayVar = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();// 进行小数点一位保留处理现实在坐标系上的数值
			canvas.drawText("" + displayVar, lift - 2, bottom + 3 - gapY * i, mTextPaint);
		}
		
		for (int i = 0; i < mData.size(); i++) 
		{
			if(i == 0)
				canvas.drawLine(lift + gapX * i, top, lift + gapX * i, bottom, mbackLinePaint);
			mTextPaint.setTextAlign(Align.CENTER);
			canvas.drawText(mData.get(i).date, lift + gapX * i, bottom + 14, mTextPaint);
			canvas.drawCircle(lift + gapX * i, bottom , 2, mPointPaint);
		}
	}

	private void drawChartLine()
	{
		while (isRunning)
		{
			
			try
			{
				drawChart(currentX);// 绘制
				currentX = currentX + 20;// 往前进
				if (currentX >= mData.size()*gapX + gapX/2)
				{
					// 如果到了终点，则清屏重来
					//clearCanvas();
					currentX = 0;
					break;
				}

				try {
					Thread.sleep(tick);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}

	void drawChart(int length)
	{
		if (length == 0)
			oldX = 0;
		Canvas canvas = sfh.lockCanvas(new Rect(oldX, 0, oldX + length, bottom));// 范围选取正确
		// Log.i("系统消息", "oldX = " + oldX + "  length = " + length);
		Paint mPointPaint = new Paint();
		mPointPaint.setAntiAlias(true);
		mPointPaint.setColor(Color.YELLOW);

		Paint mLinePaint = new Paint();// 用来画折线
		mLinePaint.setColor(Color.YELLOW);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setStrokeWidth(2);
		mLinePaint.setStyle(Style.FILL);

		long max = mData.get(0).traffic;
		long temMax = 0;
		long min = mData.get(0).traffic;
		long temMin = 0;
		float spacePX = 0f;// 平均像素值
		for (int i = 1; i < mData.size(); i++)
		{
			if (max < mData.get(i).traffic)
			{
				max = mData.get(i).traffic;
			}
			if (min > mData.get(i).traffic)
			{
				min = mData.get(i).traffic;
			}
			temMax = max;
			temMin = min;
		}
		if((temMax - temMin) <= 0)
			spacePX = 1;
		else
			spacePX = (float) ((bottom - top) / (float)(temMax - temMin))  ;// 平均每个温度值说占用的像素值

		Paint mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		// mTextPaint.setTextAlign(Align.RIGHT);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(12F);// 设置温度值的字体大小
		
		long cx = 0;
		long cy = 0;
		long dx = 0;
		long dy = 0;
		for (int j = 0; j < mData.size() ; j++)
		{
			cx = lift + gapX * j;
			cy = (long) (bottom - (mData.get(j).traffic - temMin) * spacePX);
			if(mData.size() > j+1)
			{
				dx = lift + gapX * (j + 1);
				dy = (long) (bottom - (mData.get(j + 1).traffic - temMin) * spacePX);
			}
			else
			{
				dx = cx;
				dy = cy;
			}
			canvas.drawCircle(cx, cy, 3, mPointPaint);
			canvas.drawLine(cx, cy, dx, dy, mLinePaint);
			if(j>0)
			{
				mTextPaint.setTextAlign(Align.CENTER);
				float result = (mData.get(j).traffic/unit);
				BigDecimal b = new BigDecimal(result);// 新建一个BigDecimal
				float displayVar = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();// 进行小数点一位保留处理现实在坐标系上的数值
				String textString = displayVar + unitString;
				canvas.drawText(textString, lift + gapX * j, cy - 4, mTextPaint);
			}
		}
		sfh.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像

	}

	/**
	 * 把画布擦干净，准备绘图使用。
	 */
	private void clearCanvas() {
		Canvas canvas = sfh.lockCanvas();

		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);// 清除画布

		GridDraw(canvas);

		sfh.unlockCanvasAndPost(canvas);
	}
	
	private void switchUnit(long max)
	{
		if(max/1024 > 0)
		{
			unit = 1024;
			unitString = "K";
			if(max/(1024*1024) > 0)
			{
				unit = 1024*1024;
				unitString = "M";
				if(max/(1024*1024*1024) > 0)
				{
					unit = 1024*1024*1024;
					unitString = "G";
				}
			}
		}
		else
		{
			unit = 1;
			unitString = "b";
		}
	}
}
