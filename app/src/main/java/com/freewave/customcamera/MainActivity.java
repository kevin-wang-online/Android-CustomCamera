package com.freewave.customcamera;

import com.freewave.horizontallistview.ui.HorizontalListView;
import com.freewave.horizontallistview.ui.HorizontalListViewAdapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import android.media.ExifInterface;
import android.provider.MediaStore;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.util.Log;

import com.freewave.customalbum.ImgFileListActivity;

public class MainActivity extends Activity {

    //private View layout;
    private Camera camera;
    private Camera.Parameters parameters = null;
    private SurfaceView surfaceView;

    private Boolean flashStatus;

    //Horizontal List View
    HorizontalListView hListView;
    HorizontalListViewAdapter hListViewAdapter;
    ImageView previewImg;
    View olderSelectView = null;

    //图片数组
    List<Bitmap> imageList;
    //图片名称集合
    List<String> imageTitleList;

    //相册图片地址数据
    ArrayList<String> listFileFormAlbum = new ArrayList<String>();

    Bundle bundle = null; // 声明一个Bundle对象，用来存储数据
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //状态栏透明
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //闪光灯状态
        flashStatus = false;

        // 显示界面
        setContentView(R.layout.activity_main);

        //设置采集照片视图
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);

        //设置Surface类型
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 屏幕常亮
        surfaceView.getHolder().setKeepScreenOn(true);
        //为SurfaceView的句柄添加一个回调函数
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        //设置焦点
        surfaceView.setFocusable(true);

        //初始化水平ListView
        initHorizontalListViewUI();

        //加载相册中获取到的图片
        initCustonAlbumsActivityFile();
    }

    /**
     * 初始化相册图片
     *
     */
    public void initCustonAlbumsActivityFile(){

        Bundle bundle= getIntent().getExtras();

        if (bundle!=null) {

            if (bundle.getStringArrayList("files")!=null) {

                listFileFormAlbum = bundle.getStringArrayList("files");

                if(listFileFormAlbum.size() != 0){

                    for(int index = 0; index < listFileFormAlbum.size(); index ++){

                        try{

                            String filePath = listFileFormAlbum.get(index);

                            Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                            handleOpenPictureFromAlbum(bitmap);
                        }
                        catch(Exception e) {

                            e.printStackTrace();

                        }


                    }
                }
            }
        }
    }

    /**
     * 初始化水平ListView
     *
     */
    public void initHorizontalListViewUI(){

        hListView = (HorizontalListView)findViewById(R.id.horizon_listview);

        //初始化图片标题集合
        imageTitleList = new ArrayList<String>();

        //初始化图片集合
        imageList = new ArrayList<Bitmap>();

        hListViewAdapter = new HorizontalListViewAdapter(getApplicationContext(),imageTitleList,imageList);

        hListView.setAdapter(hListViewAdapter);
        hListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // image item clicked
                hListViewAdapter.deleteSelectIndex(position);
                hListViewAdapter.notifyDataSetChanged();

                listFileFormAlbum.remove(position);
            }

        });

    }

    /**
     * 按钮被点击触发的事件
     *
     */
    public void btnOnclick(View v) {
        if (camera != null) {
            switch (v.getId()) {
                case R.id.takepicture:
                    // 拍照
                    camera.takePicture(null, null, new MyPictureCallback());
                    break;
            }
        }
    }

    /**
     * 后退按钮被点击触发的事件
     *
     * @param v
     */
    public void navigationBackButtonClicked(View v){

        finish();

    }

    /**
     * 下一步按钮被点击触发的事件
     *
     */
    public void navigationNextButtonClicked(View v){



    }

    /**
     * 相册按钮被点击触发的事件
     *
     */
    public void albumButtonClicked(View v){

        Intent intent = new Intent(this,ImgFileListActivity.class);

        Bundle bundle=new Bundle();

        bundle.putStringArrayList("images", listFileFormAlbum);

        intent.putExtras(bundle);

        startActivity(intent);
    }

    /**
     * 闪光灯按钮被点击触发的事件
     *
     */
    public void flashButtonClicked(View v) {

        parameters=camera.getParameters();

        if(flashStatus){
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            flashStatus = false;
            Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.cameraflash);
            this.findViewById(R.id.openflash).setBackground(drawable);
        }
        else{
            parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
            flashStatus = true;
            Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.cameraflashblue);
            this.findViewById(R.id.openflash).setBackground(drawable);
        }

        camera.setParameters(parameters);
    }

    private final class MyPictureCallback implements PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {

                //处理照片shu数据
                handleCameraTakePicture(data);

                //初始化Bundle对象
                bundle = new Bundle();

                //将图片字节数据保存在bundle当中，实现数据交换
                bundle.putByteArray("bytes", data);

                //图片数组保存到SD卡中
                saveToSDCard(data);

                //弹出层提示成功
                Toast.makeText(getApplicationContext(), "456", Toast.LENGTH_SHORT).show();

                //拍完照后，开始预览
                camera.startPreview();

            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }

    //处理相机照相返回的图片数据
    public void handleCameraTakePicture(byte[] data)
    {
        //转换数据为图片
        Bitmap takeBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        //矩阵变换
        Matrix matrix = new Matrix();

        //旋转
        matrix.setRotate(90,takeBitmap.getWidth()/2,takeBitmap.getHeight()/2);

        Bitmap newSource = Bitmap.createBitmap(takeBitmap, 0, 0, takeBitmap.getWidth(), takeBitmap.getHeight(), matrix, true);

        int width = getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);
        int height = getApplicationContext().getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);

        Bitmap thumBitmap = ThumbnailUtils.extractThumbnail(newSource, width, height);

//        System.out.println("======2592===========176===========200=========");
//        System.out.println("获取照片的宽度:" + thumBitmap.getWidth());
//        System.out.println("获取照片的高度" + thumBitmap.getHeight());
//        System.out.println("======1994===========144===========200=========");

        //添加照片到HorizontalListView当中
        hListViewAdapter.addNewImageView(thumBitmap, "img");
        hListViewAdapter.notifyDataSetChanged();
    }

    //处理相册返回的图片数据
    public void handleOpenPictureFromAlbum(Bitmap bitmap)
    {
        int width = getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);
        int height = getApplicationContext().getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);

        Bitmap thumBitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);

//        System.out.println("======2592===========176===========200=========");
//        System.out.println("获取照片的宽度:" + thumBitmap.getWidth());
//        System.out.println("获取照片的高度" + thumBitmap.getHeight());
//        System.out.println("======1994===========144===========200=========");

        //添加照片到HorizontalListView当中
        hListViewAdapter.addNewImageView(thumBitmap, "img");
        hListViewAdapter.notifyDataSetChanged();
    }

    /**
     * 将拍下来的照片存放在SD卡中
     * @param data
     * @throws IOException
     */
    public void saveToSDCard(byte[] data) throws IOException
    {
        //格式化写入日期
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间

        //格式化日期名称
        String filename = format.format(date) + ".jpg";

        //获取SD卡地址路径
        File fileFolder = new File(Environment.getExternalStorageDirectory() + "/Melo/");

        //图片文件路径
        String filePath = Environment.getExternalStorageDirectory() + "/Melo/" + filename;

        listFileFormAlbum.add(filePath);

        // 如果目录不存在，则创建一个名为"finger"的目录
        if (!fileFolder.exists()) {
            fileFolder.mkdir();
        }

        //生成写入文件
        File jpgFile = new File(fileFolder, filename);

        // 文件输出流
        FileOutputStream outputStream = new FileOutputStream(jpgFile);

        // 写入sd卡中
        outputStream.write(data);

        // 关闭输出流
        outputStream.close();
    }

    private final class SurfaceCallback implements Callback {

        // 拍照状态变化时调用该方法
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            parameters = camera.getParameters(); // 获取各项参数
            parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
            parameters.setPreviewFrameRate(15);  //设置每秒显示4帧
            parameters.setJpegQuality(100); // 设置照片质量
            parameters.setPreviewSize(width, height); // 设置预览大小
            parameters.setPictureSize(width, height); // 设置保存的图片尺寸

            surfaceView.getHolder().setFixedSize(width,height);

            //实现自动对焦
            camera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        initCamera();//实现相机的参数初始化
                        camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                    }
                }

            });

        }

        // 开始拍照时调用该方法
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open(); // 打开摄像头

                parameters = camera.getParameters(); // 获取各项参数

                //System.out.println("========================");
                //System.out.println(surfaceView.getWidth() + " " + surfaceView.getHeight());
                //System.out.println("========================");

                parameters.setPreviewFrameRate(15);  //设置每秒显示4帧
                parameters.setJpegQuality(100); // 设置照片质量
                parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());
                parameters.setPreviewSize(surfaceView.getWidth(), surfaceView.getHeight());

                surfaceView.getHolder().setFixedSize(surfaceView.getWidth(),surfaceView.getHeight());

                camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                camera.setDisplayOrientation(getPreviewDegree(MainActivity.this));
                camera.startPreview(); // 开始预览
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 停止拍照时调用该方法
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.release(); // 释放照相机
                camera = null;
            }
        }
    }

    //相机参数的初始化设置
    private void initCamera()
    {
        parameters=camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //1连续对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
        camera.startPreview();
        // 2如果要实现连续的自动对焦，这一句必须加上
        camera.cancelAutoFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: // 按下拍照按钮
                if (camera != null && event.getRepeatCount() == 0) {
                    // 拍照
                    //注：调用takePicture()方法进行拍照是传入了一个PictureCallback对象——当程序获取了拍照所得的图片数据之后
                    //，PictureCallback对象将会被回调，该对象可以负责对相片进行保存或传入网络
                    camera.takePicture(null, null, new MyPictureCallback());
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }
}
