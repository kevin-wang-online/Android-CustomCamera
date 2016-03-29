package com.freewave.customalbum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.freewave.customcamera.R;
import com.freewave.customcamera.MainActivity;
import com.freewave.customalbum.ImgsAdapter.OnItemClickClass;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImgsActivity extends Activity {

	Bundle bundle;
	FileTraversal fileTraversal;
	GridView imgGridView;
	ImgsAdapter imgsAdapter;
	LinearLayout select_layout;
	Util util;
	RelativeLayout relativeLayout2;
	HashMap<Integer, ImageView> hashImage;
	Button choise_button;
	ArrayList<String> filelist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		//状态栏透明
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

		setContentView(R.layout.photogrally);
		
		imgGridView=(GridView) findViewById(R.id.gridView1);
		bundle= getIntent().getExtras();
		fileTraversal=bundle.getParcelable("data");
		imgsAdapter=new ImgsAdapter(this, fileTraversal.filecontent,onItemClickClass);
		imgGridView.setAdapter(imgsAdapter);
		select_layout=(LinearLayout) findViewById(R.id.selected_image_layout);
		relativeLayout2=(RelativeLayout) findViewById(R.id.relativeLayout2);
		choise_button=(Button) findViewById(R.id.button3);
		hashImage=new HashMap<Integer, ImageView>();
		filelist=new ArrayList<String>();
		util=new Util(this);

		//添加上一页的图片地址到地址集合当中
		addCameraImagesIntoFileList();
	}
	
	class BottomImgIcon implements OnItemClickListener {
		
		int index;
		public BottomImgIcon(int index) {
			this.index=index;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
		}
	}

	//添加上一页的图片地址到地址集合当中
	public void addCameraImagesIntoFileList(){

		Bundle bundle= getIntent().getExtras();

		if (bundle!=null) {

			if (bundle.getStringArrayList("imageList")!= null || bundle.getStringArrayList("imageList").size() != 0) {

				for(int index = 0; index < bundle.getStringArrayList("imageList").size(); index ++){
					filelist.add(bundle.getStringArrayList("imageList").get(index));
				}
			}
		}

	}

	@SuppressLint("NewApi")
	public ImageView iconImage(String filepath,int index,CheckBox checkBox) throws FileNotFoundException {
		LinearLayout.LayoutParams params=new LayoutParams(relativeLayout2.getMeasuredHeight()-10, relativeLayout2.getMeasuredHeight()-10);
		ImageView imageView=new ImageView(this);
		imageView.setLayoutParams(params);
//		imageView.setBackgroundResource(R.drawable.imgbg);
		float alpha=1;
		imageView.setAlpha(alpha);
		util.imgExcute(imageView, imgCallBack, filepath);
		imageView.setOnClickListener(new ImgOnclick(filepath, checkBox));
		return imageView;
	}
	
	ImgCallBack imgCallBack=new ImgCallBack() {
		@Override
		public void resultImgCall(ImageView imageView, Bitmap bitmap) {
			imageView.setImageBitmap(bitmap);
		}
	};
	
	class ImgOnclick implements OnClickListener {

		String filepath;
		CheckBox checkBox;

		public ImgOnclick(String filepath,CheckBox checkBox) {
			this.filepath=filepath;
			this.checkBox=checkBox;
		}

		@Override
		public void onClick(View arg0) {
			checkBox.setChecked(false);
			select_layout.removeView(arg0);
			choise_button.setText("已选择("+select_layout.getChildCount()+")张");
			filelist.remove(filepath);
		}
	}
	
	ImgsAdapter.OnItemClickClass onItemClickClass=new OnItemClickClass() {
		@Override
		public void OnItemClick(View v, int Position, CheckBox checkBox) {

			// 获取文件地址
			String filapath=fileTraversal.filecontent.get(Position);

			if (checkBox.isChecked())
			{
				//取消文件选取
				//取消选中状态
				checkBox.setChecked(false);
				//底部显示器删除图片
				select_layout.removeView(hashImage.get(Position));
				//删除文件地址
				filelist.remove(filapath);
				//更新底部显示器文字
				choise_button.setText("已选择("+select_layout.getChildCount()+")张");
			}
			else
			{
				try
				{
					checkBox.setChecked(true);
					Log.i("img", "img choise position->" + Position);
					ImageView imageView=iconImage(filapath, Position,checkBox);

					if (imageView !=null)
					{
						hashImage.put(Position, imageView);
						filelist.add(filapath);
						select_layout.addView(imageView);
						choise_button.setText("已选择("+select_layout.getChildCount()+")张");
					}
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
	};
	
	public void tobreak(View view){
		finish();
	}

	//保存图片列表文件,返回主界面
	public void sendfiles(View view){

		Intent intent =new Intent(this, MainActivity.class);

		Bundle bundle=new Bundle();

		bundle.putStringArrayList("files", filelist);

		intent.putExtras(bundle);

		startActivity(intent);

	}
}
