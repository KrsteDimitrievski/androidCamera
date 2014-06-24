package com.example.photocapture.app;

import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private String TAG = "MainActivity";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Handler mHandler = new Handler();
    private int numberOfPhotosValue = 1;
    private int frameCount = 1;
    private int delayInMillisecondsValue = 1000;
    //private PowerManager powerManager;
    //private PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        //wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startUpPreview();
    }

    public void setNumberOfPhotosValue(int input){
        if (input < 1){
            numberOfPhotosValue = 1;
        }
        else{
            numberOfPhotosValue = input;
        }
    }

    public void setDelayInMillisecondsValue(int input){
        if (input < 1000){
            delayInMillisecondsValue = 1000;
        }
        else{
            delayInMillisecondsValue = input;
        }
    }


    public void startUpPreview(){
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    public void getTimelapse(View view) {
        TextView progressCount = (TextView) findViewById(R.id.progressCount);
        progressCount.setText("Starting");
        EditText numberOfPhotosText = (EditText) findViewById(R.id.numberOfPhotos);
        EditText delayInSecondsText = (EditText) findViewById(R.id.delayInSeconds);
        String numberOfPhotosString = numberOfPhotosText.getText().toString();
        String delayInSecondsString = delayInSecondsText.getText().toString();
        int numberOfPhotosValue = 1;
        int delayInMillisecondsValue = 1000;
        if (numberOfPhotosString == null) {
            numberOfPhotosValue = 1;
        } else {
            setNumberOfPhotosValue(Integer.parseInt(numberOfPhotosString));
        }
        if (delayInSecondsString == null) {
            delayInMillisecondsValue = 1000;
        } else {
            setDelayInMillisecondsValue(Integer.parseInt(delayInSecondsString) * 1000);
        }
        //wl.acquire();
        mCamera.takePicture(null, null, mPicture);

    }

    private Runnable runTakePicture() {
        return new Runnable() {
            public void run() {
                mCamera.takePicture(null, null, mPicture);
            }
        };
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile;
            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: "); // +
                        //e.getMessage());
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            TextView progressCount = (TextView) findViewById(R.id.progressCount);
            progressCount.setText(String.valueOf(frameCount));
            System.out.println("The number of photos detected value by mPicture " + String.valueOf(numberOfPhotosValue));
            System.out.println("The current frame is " + String.valueOf(frameCount));
            frameCount++;
            if (frameCount <= numberOfPhotosValue){
                mCamera.startPreview();
                mHandler.postDelayed(runTakePicture(), delayInMillisecondsValue);
            }
            else {
                progressCount.setText(String.valueOf("Done"));
                mCamera.startPreview();
            //    wl.release();
            }
        }
    };

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

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

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}
