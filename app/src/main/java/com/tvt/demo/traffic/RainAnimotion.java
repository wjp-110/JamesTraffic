package com.tvt.demo.traffic;

/*
 * 用于实现柱状图的动态效果
 * */
import java.math.BigDecimal;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RainAnimotion extends SurfaceView implements SurfaceHolder.Callback
{
	private int currentX;
	private int score;

	private int oldX;

	private SurfaceHolder sfh;

	private boolean isRunning = true;

	private int tick = 20; // 时间间隔(ms)
	private int bottom = 150; // 坐标系地段距离框架顶端的距离
	private int top = 30; // 坐标系顶端距离框架顶端框的距离
	private int lift = 38; // 坐标系左边距离框架左边框的距离
	static int right = 30; // 坐标系右边距离框架左边的距离(!)
	static int gapX = 50; // 两根竖线间的间隙(!)
	private int gapY = 20; // 两根横线间的间隙
	
	private float unit = 1f;
	private String unitString = "b";

	private ArrayList<TrafficInfo> mData = new ArrayList<TrafficInfo>();
	
	public RainAnimotion(Context context)
	{
		super(context);
		init(context);
	}

	// 在这里初始化才是最初始化的。
	public RainAnimotion(Context context, AttributeSet atr)
	{
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
		for (int i = 1; i < mData.size(); i++) {
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

		Canvas canvas = sfh.lockCanvas();

		Paint mbackLinePaint = new Paint();// 用来画坐标系了
		mbackLinePaint.setColor(Color.DKGRAY);
		mbackLinePaint.setAntiAlias(true);
		mbackLinePaint.setStrokeWidth(1);
		mbackLinePaint.setStyle(Style.FILL);

		Paint mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(Color.DKGRAY);
		mTextPaint.setTextSize(12F);// 设置温度值的字体大小
		
		Paint mPointPaint = new Paint();
		mPointPaint.setAntiAlias(true);
		mPointPaint.setColor(Color.DKGRAY);
		
		// 绘制坐标系
		for (int i = 0; i < 8; i++) 
		{
			if(i == 7)
				canvas.drawLine(lift, bottom, lift + gapX * mData.size(),bottom, mbackLinePaint);
			else
				canvas.drawCircle(lift, top + gapY * i + 4, 2, mPointPaint);
//				canvas.drawLine(lift, top + gapY * i, lift + gapX * mData.size(), top + gapY * i, mbackLinePaint);
			mTextPaint.setTextAlign(Align.RIGHT);
			if (temMax < 0.8) 
			{
				float result = 0.1f * i;
				canvas.drawText("" + result, lift - 2, bottom + 3 - gapY * i,mTextPaint);
			} 
			else
			{
				float result = temMin + space * i;// 精确的各个节点的值
				BigDecimal b = new BigDecimal(result);// 新建一个BigDecimal
				float displayVar = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();// 进行小数点一位保留处理现实在坐标系上的数值
				canvas.drawText("" + displayVar, lift - 2, bottom + 3 - gapY * i, mTextPaint);
			}
		}
		for (int i = 0; i < mData.size(); i++) 
		{
			if(i == 0)
				canvas.drawLine(lift + gapX * i, top, lift + gapX * i, bottom,mbackLinePaint);
			mTextPaint.setTextAlign(Align.CENTER);
			canvas.drawText(mData.get(i).date, lift + gapX * i + gapX / 2, bottom + 14,mTextPaint);
		}
		sfh.unlockCanvasAndPost(canvas);
	}

	protected void GridDraw(Canvas canvas) {
		if (canvas == null) {
			return;
		}
		long max = mData.get(0).traffic;
		long temMax = max;
		long min = max;
		long temMin = max;
		float space = 0f;// 平均值
		for (int i = 1; i < mData.size(); i++) {
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
		mbackLinePaint.setColor(Color.DKGRAY);
		mbackLinePaint.setAntiAlias(true);
		mbackLinePaint.setStrokeWidth(1);
		mbackLinePaint.setStyle(Style.FILL);

		Paint mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		// mTextPaint.setTextAlign(Align.RIGHT);
		mTextPaint.setColor(Color.DKGRAY);
		mTextPaint.setTextSize(12F);// 设置温度值的字体大小
		
		Paint mPointPaint = new Paint();
		mPointPaint.setAntiAlias(true);
		mPointPaint.setColor(Color.DKGRAY);
		// 绘制坐标系
		for (int i = 0; i < 8; i++) {
			if(i == 7)
				canvas.drawLine(lift, bottom, lift + gapX * mData.size(),bottom, mbackLinePaint);
//				canvas.drawLine(lift, top + gapY * i, lift + gapX *  mData.size(), top + gapY * i, mbackLinePaint);
			else
				canvas.drawCircle(lift, top + gapY * i + 4, 2, mPointPaint);
			mTextPaint.setTextAlign(Align.RIGHT);
			if (temMax < 0.8) {
				float result = 0.1f * i;
				canvas.drawText("" + result, lift - 2, bottom + 3 - gapY * i,
						mTextPaint);
			} else {
				float result = temMin + space * i;// 精确的各个节点的值
				BigDecimal b = new BigDecimal(result);// 新建一个BigDecimal
				float displayVar = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();// 进行小数点一位保留处理现实在坐标系上的数值
				canvas.drawText("" + displayVar, lift - 2, bottom + 3 - gapY * i,mTextPaint);
			}
		}
		for (int i = 0; i <  mData.size(); i++) {
			if(i == 0)
				canvas.drawLine(lift + gapX * i, top, lift + gapX * i, bottom, mbackLinePaint);
			mTextPaint.setTextAlign(Align.CENTER);
			canvas.drawText(mData.get(i).date, lift + gapX * i + gapX / 2, bottom + 14, mTextPaint);
		}
	}

	private void drawChartLine()
	{
		while (isRunning)
		{
			if (currentX == 0)
				oldX = bottom + top;
			Canvas canvas = sfh.lockCanvas(new Rect(lift, oldX - currentX, lift + gapX * mData.size(), oldX));// 范围选取正确
			Log.i("系统消息", "oldX = " + oldX + "  currentX = " + currentX);
			try
			{
				// score = bottom - currentX;
				// drawChart(score);// 绘制
				drawChart(canvas);// 绘制
				currentX = currentX + 10;// 往前进

				if (currentX >= bottom + top) {
					// 如果到了终点，则清屏重来
					break;
				}

				try {
					Thread.sleep(tick);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			finally{
				sfh.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
			}
		}
	}

	void drawChart(Canvas canvas)
	{

		Paint mPointPaint = new Paint();
		mPointPaint.setAntiAlias(true);
		mPointPaint.setColor(Color.YELLOW);

		Paint mLinePaint = new Paint();// 用来画折线
		mLinePaint.setColor(Color.YELLOW);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setStrokeWidth(2);
		mLinePaint.setStyle(Style.FILL);

		long max = mData.get(0).traffic;
		long temMax = max;
		long min = max;
		long temMin = max;
		float spacePX = 0f;// 平均像素值
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
		if((temMax - temMin) <= 0)
			spacePX = 1;
		else
			spacePX = (float) ((bottom - top) / (float)(temMax - temMin))  ;// 平均每个温度值说占用的像素值
		
		Paint mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		// mTextPaint.setTextAlign(Align.RIGHT);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(12F);// 设置温度值的字体大小
		
		float cx = 0f;
		float cy = 0f;
		float dx = 0f;
		float dy = 0f;
		for (int j = 0; j < mData.size(); j++)
		{
			cx = lift + gapX * j;
			cy = bottom - (mData.get(j).traffic - temMin) * spacePX;
			dx = lift + gapX * (j + 1);
			dy = bottom - (mData.get(j).traffic - temMin) * gapY * 10;

			if (mData.get(j).traffic == 0) 
			{
				mTextPaint.setTextAlign(Align.CENTER);
				String textString = mData.get(j).traffic/1024 + "KB";
				canvas.drawText(textString, cx + gapX / 2, cy - 10, mTextPaint);
				canvas.drawRect(new RectF(cx + 5, bottom - 5, dx - 5, bottom), mLinePaint);// 当雨量值是0时，绘制2px的矩形，表示这里有值
			} 
			else 
			{
				mTextPaint.setTextAlign(Align.CENTER);
				float result = (mData.get(j).traffic/unit);
				BigDecimal b = new BigDecimal(result);// 新建一个BigDecimal
				float displayVar = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();// 进行小数点一位保留处理现实在坐标系上的数值
				String textString = displayVar + unitString;
				canvas.drawText(textString, cx + gapX / 2, cy - 4, mTextPaint);
				canvas.drawRect(new RectF(cx + 5, cy, dx - 5, bottom), mLinePaint);
			}

		}
		
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
