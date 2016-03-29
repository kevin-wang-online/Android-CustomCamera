package com.freewave.horizontallistview.customborderimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.freewave.customcamera.R;

/**
 * 自定义View，实现圆角，圆形等效果
 * 
 * @author zhy
 * 
 */
public class CustomImageView extends View
{

	/**
	 * TYPE_CIRCLE / TYPE_ROUND
	 */
	private int type;
	private static final int TYPE_CIRCLE = 0;
	private static final int TYPE_ROUND = 1;

	/**
	 * 图片
	 */
	private Bitmap mSrc;

	/**
	 * 圆角的大小
	 */
	private int mRadius;

	/**
	 * 控件的宽度
	 */
	private int mWidth;
	/**
	 * 控件的高度
	 */
	private int mHeight;

	public void setImageBitmap(Bitmap bitmap)
	{
		// 设置外部图片资源
		mSrc = null;
		mSrc = bitmap;
	}

	public CustomImageView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public CustomImageView(Context context)
	{
		this(context, null);
	}

	/**
	 * 初始化一些自定义的参数
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CustomImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.CustomImageView, defStyle, 0);

		int n = a.getIndexCount();

		for (int i = 0; i < n; i++)
		{
			int attr = a.getIndex(i);
			switch (attr)
			{
			case R.styleable.CustomImageView_src:
				mSrc = BitmapFactory.decodeResource(getResources(),
						a.getResourceId(attr, 0));
				break;
			case R.styleable.CustomImageView_type:
				type = a.getInt(attr, 0);// 默认为Circle
				break;
			case R.styleable.CustomImageView_borderRadius:
				mRadius = a.getDimensionPixelSize(attr, (int) TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
								getResources().getDisplayMetrics()));// 默认为10DP
				break;
			}
		}
		a.recycle();
	}

	/**
	 * 计算控件的高度和宽度
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		/**
		 * 设置宽度
		 */
		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);

		if (specMode == MeasureSpec.EXACTLY)// match_parent , accurate
		{
			mWidth = specSize;
		} else
		{
			// 由图片决定的宽
			int desireByImg = getPaddingLeft() + getPaddingRight()
					+ mSrc.getWidth();
			if (specMode == MeasureSpec.AT_MOST)// wrap_content
			{
				mWidth = Math.min(desireByImg, specSize);
			} else

				mWidth = desireByImg;
		}

		mWidth = 170;

		/***
		 * 设置高度
		 */

		specMode = MeasureSpec.getMode(heightMeasureSpec);
		specSize = MeasureSpec.getSize(heightMeasureSpec);
		if (specMode == MeasureSpec.EXACTLY)// match_parent , accurate
		{
			mHeight = specSize;
		} else
		{
			int desire = getPaddingTop() + getPaddingBottom()
					+ mSrc.getHeight();

			if (specMode == MeasureSpec.AT_MOST)// wrap_content
			{
				mHeight = Math.min(desire, specSize);
			} else
				mHeight = desire;
		}

		mHeight = 170;

		setMeasuredDimension(mWidth, mHeight);

	}

	/**
	 * 绘制
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		switch (type)
		{
		// 如果是TYPE_CIRCLE绘制圆形
		case TYPE_CIRCLE:
			int min = Math.min(mWidth, mHeight);
			/**
			 * 长度如果不一致，按小的值进行压缩
			 */
			mSrc = Bitmap.createScaledBitmap(mSrc, min, min, false);
			canvas.drawBitmap(createCircleImage(mSrc, min), 0, 0, null);
			break;
		case TYPE_ROUND:
			canvas.drawBitmap(createRoundConerImage(mSrc), 0, 0, null);
			break;

		}

	}

	/**
	 * 根据原图和变长绘制圆形图片
	 * 
	 * @param source
	 * @param min
	 * @return
	 */
	private Bitmap createCircleImage(Bitmap source, int min)
	{
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		Bitmap target = Bitmap.createBitmap(min, min, Config.ARGB_8888);
		/**
		 * 产生一个同样大小的画布
		 */
		Canvas canvas = new Canvas(target);
		/**
		 * 首先绘制圆形
		 */
		canvas.drawCircle(min / 2, min / 2, min / 2, paint);
		/**
		 * 使用SRC_IN，参考上面的说明
		 */
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		/**
		 * 绘制图片
		 */
		canvas.drawBitmap(source, 0, 0, paint);
		return target;
	}

	/**
	 * 根据原图添加圆角
	 * 
	 * @param source
	 * @return
	 */
	private Bitmap createRoundConerImage(Bitmap source)
	{
		final Paint paint = new Paint();

		//设置画笔无锯齿
		paint.setAntiAlias(true);

		Bitmap target = Bitmap.createBitmap(170, 170, Config.ARGB_8888);

		// 构造画布
		Canvas canvas = new Canvas(target);

		//画背景圆角
		RectF rect = new RectF(0, 0, 170, 170);
		canvas.drawRoundRect(rect, mRadius, mRadius, paint);

		//两个绘制的效果叠加后取交集展现后图
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

		//图片缩放
		int width = source.getWidth();
		int height = source.getHeight();

		float scaleWidth = ((float) 170) / width;
		float scaleHeight = ((float) 170) / height;

		Matrix matrix = new Matrix();
		//缩放
		matrix.postScale(scaleWidth, scaleHeight);
		//旋转
		//matrix.setRotate(90,width/2,height/2);

		Bitmap newSource = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);

		//图片画上去
		canvas.drawBitmap(newSource, 0, 0, paint);

		return target;
	}
}
