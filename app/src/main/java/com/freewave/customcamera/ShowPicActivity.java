package com.freewave.customcamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by fwm68 on 15/11/11.
 */
public class ShowPicActivity extends Activity {

    private ImageView ivPic = null; // 显示图片控件


    /**
     * Activity在创建的时候回调的函数 主要用来初始化一些变量
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showpic);
        ivPic = (ImageView) findViewById(R.id.ivPic);
        setImageBitmap(getImageFormBundle());

    }

    /**
     * 将MainActivity传过来的图片显示在界面当中
     *
     * @param bytes
     */
    public void setImageBitmap(byte[] bytes) {
        Bitmap cameraBitmap = byte2Bitmap();
        // 根据拍摄的方向旋转图像（纵向拍摄时要需要将图像选择90度)
        Matrix matrix = new Matrix();
        matrix.setRotate(MainActivity.getPreviewDegree(this));
        cameraBitmap = Bitmap
                .createBitmap(cameraBitmap, 0, 0, cameraBitmap.getWidth(),
                        cameraBitmap.getHeight(), matrix, true);
        ivPic.setImageBitmap(cameraBitmap);
    }

    /**
     * 从Bundle对象中获取数据
     *
     * @return
     */
    public byte[] getImageFormBundle() {
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        byte[] bytes = data.getByteArray("bytes");
        return bytes;
    }

    /**
     * 将字节数组的图形数据转换为Bitmap
     *
     * @return
     */
    private Bitmap byte2Bitmap() {
        byte[] data = getImageFormBundle();
        // 将byte数组转换成Bitmap对象
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }

}
