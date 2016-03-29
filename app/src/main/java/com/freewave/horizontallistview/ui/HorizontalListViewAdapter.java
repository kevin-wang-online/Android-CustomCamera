package com.freewave.horizontallistview.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.freewave.customcamera.R;
import com.freewave.horizontallistview.util.BitmapUtil;

import java.util.List;

import com.freewave.horizontallistview.customborderimageview.CustomImageView;

public class HorizontalListViewAdapter extends BaseAdapter {

	private List<Bitmap> mIconIDs;
	private List<String> mTitles;

	private Context mContext;
	private LayoutInflater mInflater;
	Bitmap iconBitmap;
	private int selectIndex = -1;

	public HorizontalListViewAdapter(Context context, List<String> titles, List<Bitmap> ids){
		this.mContext = context;
		this.mIconIDs = ids;
		this.mTitles = titles;
		mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
	}

	public void setmIconIDs(List<Bitmap> mIconIDs)
	{
		this.mIconIDs = mIconIDs;
	}

	public void setmTitles(List<String> mTitles)
	{
		this.mTitles = mTitles;
	}

	@Override
	public int getCount() {
		return mIconIDs.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if(convertView==null)
		{
			holder = new ViewHolder();

			//获取布局当中的水平方向滚动的ListView
			convertView = mInflater.inflate(R.layout.horizontal_list_item, null);

			//设置图片视图
			//holder.mImage=(ImageView)convertView.findViewById(R.id.img_list_item);

			//设置显示自定义图片视图
			holder.customImage = (CustomImageView)convertView.findViewById(R.id.img_list_item);

			//设置图片下文本内容
			//holder.mTitle=(TextView)convertView.findViewById(R.id.text_list_item);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}

		//设置选中效果
		if(position == selectIndex)
		{
			convertView.setSelected(true);
		}
		else{
			convertView.setSelected(false);
		}

		//设置图片下方文本内容
		//holder.mTitle.setText(mTitles[position]);

		//根据图片数组当中的图片名称获取图片资源
		iconBitmap = mIconIDs.get(position);

		//设置图片
		//holder.mImage.setImageBitmap(iconBitmap);

		holder.customImage.setImageBitmap(iconBitmap);

		return convertView;
	}

	private static class ViewHolder {
		private TextView mTitle ;
		private ImageView mImage;
		private CustomImageView customImage;
	}

	private Bitmap getPropThumnail(int id){
		Drawable d = mContext.getResources().getDrawable(id);
		Bitmap b = BitmapUtil.drawableToBitmap(d);

		int w = mContext.getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);
		int h = mContext.getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);
		
		Bitmap thumBitmap = ThumbnailUtils.extractThumbnail(b, w, h);
		
		return thumBitmap;
	}

	public void setSelectIndex(int i){
		selectIndex = i;
	}

	//删除选中的图片
	public void deleteSelectIndex(int i){

		mIconIDs.remove(i);
		mTitles.remove(i);

	}

	public void addNewImageView(Bitmap bitmap, String title){

		mIconIDs.add(bitmap);
		mTitles.add(title);

	}

}