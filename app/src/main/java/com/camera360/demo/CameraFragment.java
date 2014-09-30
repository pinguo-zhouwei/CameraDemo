package com.camera360.demo;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by zhouwei on 14-9-29.
 */
public class CameraFragment extends Fragment {
    public String TAG = "CameraFragment";
    private Camera mCamera;
    private CameraPreview mPreview;
    private View mCameraView ;
    public static final int MEDIA_TYPE_IMAGE = 1;//保存照片
    public static final int MEDIA_TYPE_VIDEO = 2;//保存视频

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if(success){//表示对焦成功
               Log.i(TAG,"对焦成功");
            }else{
              //对焦失败
                Log.w(TAG,"对焦失败");
            }
        }
    };
    /** 通过提供一个静态方法来提供一个Fragment的实例*/
    public static  CameraFragment getInstance(){
        CameraFragment cameraFragment = new CameraFragment();
        return cameraFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        mCameraView = view;
        boolean isSafeOpen = safeCameraOpenInView(view);
        if(isSafeOpen==false){
            Log.e("CameraFragment","Camera 没有被打开");
            return view;
        }
        ImageView btnCapture = (ImageView)view.findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null,null,mPicture);
            }
        });
        return view;
    }

    private boolean safeCameraOpenInView(View view) {
        boolean qOpened = false;
        releaseCameraAndPreview();
        mCamera = getCameraInstance();
        /** 默认预览的图片为横向，我们需要将显示的Orientation旋转以下*/
        mCamera.setDisplayOrientation(90);
        qOpened = (mCamera != null);
        mPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        return qOpened;
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance

        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Clear any existing preview / camera.
     */
    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if(mPreview != null){
            mPreview.destroyDrawingCache();
            mPreview.camera = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * 当Fragment销毁的时候，需要释放相机
         */
        releaseCameraAndPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
        final String TAG ="CameraPreview";
        SurfaceHolder mHolder;
        Camera camera;
        Context context;
        int screenHeight;
        int screenWidth;


         public CameraPreview(Context context,Camera c){
             super(context);
             this.context = context;
             camera = c;
             //安装一个SurfaceHolder.Callback，在surfaceview 创建或销毁的时候得到更新。
             mHolder = getHolder();
             mHolder.addCallback(this);
             //
             mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         }
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            //surface已经创建，现在告诉相机在哪儿绘制预览view
            try {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i1, int i2, int i3) {
           //如果你的相机可以更改或旋转，在这里注意那些事件
            //保证在重新设置大小，重置格式之前停止预览view
            if(mHolder.getSurface()==null){
                //预览view不存在
                return;
            }

            //在作出改变之前停止预览view
             try {
                 mCamera.stopPreview();
             }catch (Exception e){

             }
           //在这里设置preView的大小，重置大小
            //摄像头画面显示在Surface上
            if (mCamera != null) {
                getScreenSize();//获取屏幕的宽高
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                int[] a = new int[sizes.size()];
                int[] b = new int[sizes.size()];
                for (int i = 0; i < sizes.size(); i++) {
                    int supportH = sizes.get(i).height;
                    int supportW = sizes.get(i).width;
                    a[i] = Math.abs(supportW - screenHeight);
                    b[i] = Math.abs(supportH - screenWidth);
                    Log.d(TAG,"supportW:"+supportW+"supportH:"+supportH);
                }
                int minW=0,minA=a[0];
                for( int i=0; i<a.length; i++){
                    if(a[i]<=minA){
                        minW=i;
                        minA=a[i];
                    }
                }
                int minH=0,minB=b[0];
                for( int i=0; i<b.length; i++){
                    if(b[i]<minB){
                        minH=i;
                        minB=b[i];
                    }
                }
                Log.d(TAG,"result="+sizes.get(minW).width+"x"+sizes.get(minH).height);
                List<Integer> list = parameters.getSupportedPreviewFrameRates();
                parameters.setPreviewSize(sizes.get(minW).width,sizes.get(minH).height); // 设置预览图像大小
                parameters.setPictureSize(sizes.get(minW).width,sizes.get(minH).height);//设置图片的大小
                parameters.setPreviewFrameRate(list.get(list.size() - 1));
                mCamera.setParameters(parameters);
              //  mCamera.setDisplayOrientation(90);
              //  mCamera.startPreview();

            }

            //用新的设置开启Preview
            try {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
                /**
                 * 设置自动对焦
                 * 注意对焦操作要在开启预览后，不然会抛异常
                 *
                 */
                mCamera.autoFocus(autoFocusCallback);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
           //这个时候应该释放camera
            if(camera!=null){
                camera.stopPreview();
            }
        }
        public void getScreenSize() {
            WindowManager wm = (WindowManager) context.getSystemService(
                    Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            screenWidth = display.getWidth();
            screenHeight = display.getHeight();
        }
    }

    /**
     * Picture Callback 处理图片的预览和保存到文件
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            System.out.println("zhixing ...........");
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Toast.makeText(getActivity(), "Image retrieval failed.", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                // Restart the camera preview.
               safeCameraOpenInView(mCameraView);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * 自动聚焦
     */
   public void reAutoFoucus(){
       mCamera.autoFocus(autoFocusCallback);
   }
}
