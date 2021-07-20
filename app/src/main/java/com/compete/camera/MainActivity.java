package com.compete.camera;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    // 摄像头预览
    private TextureView textureView;
    // 视频帧预览
    private ImageView ivPic;
    // 按钮：点击开始获取视频帧数据
    private Button btnTakePic;
    // 摄像机
    private Camera mCamera;
    // ip编辑框
    private EditText edt;
    private EditText edt2;
    // ip地址
    private String ip;
    private String ip2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.CAMERA}; // 选择你需要申请的权限
            for (int i = 0; i < permissions.length; i++) {
                int state = ContextCompat.checkSelfPermission(this, permissions[i]);
                if (state != PackageManager.PERMISSION_GRANTED) { // 判断权限的状态
                    ActivityCompat.requestPermissions(this, permissions, 200); // 申请权限
                    return;
                }
            }
        }
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        textureView.setSurfaceTextureListener(this);
        btnTakePic.setOnClickListener(v -> addCallBack());
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ip = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
        edt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ip2 = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private void initData() {
        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        if(numberOfCameras < 1){
            Toast.makeText(this, "没有相机", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initView() {
        textureView = (TextureView) findViewById(R.id.texture_view);
//        textureView.setRotation(0); // // 设置预览角度，并不改变获取到的原始数据方向(与Camera.setDisplayOrientation(0)相同)
        ivPic = findViewById(R.id.iv_pic);
        btnTakePic = findViewById(R.id.btn_takePic);
        edt = findViewById(R.id.ip);
        edt2 = findViewById(R.id.ip2);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // 打开相机 0后置 1前置
        mCamera = Camera.open(0);
        if (mCamera != null) {
            // 设置相机预览宽高，此处设置为TextureView宽高
            Camera.Parameters params = mCamera.getParameters();
            // 设置自动对焦模式
            mCamera.setParameters(params);
            try {
                // 绑定相机和预览的View
                mCamera.setPreviewTexture(surface);
                // 开始预览
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addCallBack() {
        if(mCamera!=null){
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try{
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if(image!=null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 10, stream);
                            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            ivPic.setImageBitmap(bmp);
                            IPC.send(bmp, ip, 8999);
                            IPC.send(bmp, ip2, 8888);
                            stream.close();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

}