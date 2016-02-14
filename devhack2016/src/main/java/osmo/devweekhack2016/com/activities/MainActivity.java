package osmo.devweekhack2016.com.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import butterknife.Bind;
import butterknife.ButterKnife;
import dji.sdk.AirLink.DJILBAirLink;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Codec.DJICodecManager;
import dji.sdk.base.DJIBaseProduct;
import osmo.devweekhack2016.com.R;
import osmo.devweekhack2016.com.api.ApiUtility;
import osmo.devweekhack2016.com.application.EmotilizeApplication;
import osmo.devweekhack2016.com.image.EmotionApiUtil;
import osmo.devweekhack2016.com.image.ImageHelper;
import osmo.devweekhack2016.com.image.OsmoUtil;
import osmo.devweekhack2016.com.interfaces.OnDeviceConnected;
import osmo.devweekhack2016.com.interfaces.OnEmotionResultsUpdated;
import osmo.devweekhack2016.com.model.Face;
import osmo.devweekhack2016.com.fragments.MainFragment;
import osmo.devweekhack2016.com.ui.ToastUtil;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, OnDeviceConnected, OnEmotionResultsUpdated {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static int INTERVAL_CHECK = 10000; // Sets interval to 30 seconds.

    private boolean useOsmoCamera = false; // TRUE: OSMO CAMERA | FALSE: ANDROID CAMERA
    private MainFragment currentFragment;

    private Bitmap currentBitmapFrame = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888); // Stores the current Bitmap frame.
    private EmotionServiceClient client;
    private Handler dataImageHandler = new Handler(); // Handler for the data thread.

    private Camera mCamera;
    private DJIBaseProduct djiProduct = null;
    private DJICamera djiCamera = null;

    protected DJICodecManager mCodecManager = null; // Codec for video live view.
    protected DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;
    protected DJILBAirLink.DJIOnReceivedVideoCallback mOnReceivedVideoCallback = null;

    private List<Face> faceList = new LinkedList<>();

    @Bind(R.id.video_preview_surface) TextureView videoTextureView;
    @Bind(R.id.testImageView) ImageView testImageView;

    /** ACTIVITY LIFECYCLE METHODS _____________________________________________________________ **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {

            MainFragment mainFragment = new MainFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, mainFragment)
                    .commit();
        }

        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.emotion_api_key));
        }

        initVideoProcessing();
        initBroadcastReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();

        // OSMO CAMERA MODE:
        if (useOsmoCamera) {
            OsmoUtil.initDevice(mReceivedVideoDataCallBack, mOnReceivedVideoCallback, this);
        } else {
            startImageProcessingThread(true); // Starts the image processing thread.
        }

        if (videoTextureView == null) {
            Log.e(LOG_TAG, "onResume(): videoTextureView is null.");
        }
    }

    @Override
    public void onPause() {
        startImageProcessingThread(false); // Stops the image processing thread.

        // OSMO CAMERA MODE:
        if (useOsmoCamera) {
            OsmoUtil.uninitDevice(this);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        // OSMO CAMERA MODE:
        if (useOsmoCamera) {
            OsmoUtil.uninitDevice(this);
        }

        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    /** OVERRIDE METHODS _______________________________________________________________________ **/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        // OSMO CAMERA MODE:
        if (useOsmoCamera) {

            Log.d(LOG_TAG, "onSurfaceTextureAvailable(): Using OSMO Camera as source device.");

            if (mCodecManager == null) {
                Log.e(LOG_TAG, "onSurfaceTextureAvailable(): mCodecManager is null.");
                mCodecManager = new DJICodecManager(this, surface, width, height);
            }
        }

        // ANDROID CAMERA MODE:
        else {

            Log.d(LOG_TAG, "onSurfaceTextureAvailable(): Using Android Camera as source device.");

            mCamera = Camera.open(); // Opens the camera.
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();

//            videoTextureView.setLayoutParams(new LinearLayout.LayoutParams(
//                    previewSize.width, previewSize.height, Gravity.CENTER));

            try {
                mCamera.setPreviewTexture(surface);
            }

            catch (IOException t) {
                Log.e(LOG_TAG, "onSurfaceTextureAvailable(): ERROR: I/O Exception occurred: " + t.getMessage());
            }

            mCamera.startPreview();
            videoTextureView.setAlpha(1.0f);
            videoTextureView.setRotation(90.0f);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        // OSMO CAMERA MODE:
        if (useOsmoCamera) {
            if (mCodecManager != null) {
                mCodecManager.cleanSurface();
                mCodecManager = null;
            }
        }

        // ANDROID CAMERA MODE:
        else {
            mCamera.stopPreview();
            mCamera.release();
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    /** FRAGMENT METHODS _______________________________________________________________________ **/

    public void setFragment(MainFragment fragment) {
        this.currentFragment = fragment;
    }

    /** VIDEO METHODS __________________________________________________________________________ **/

    private void initVideoProcessing() {

        if (null != videoTextureView) {
            videoTextureView.setSurfaceTextureListener(this);
        }

        // OSMO CAMERA MODE:
        if (useOsmoCamera) {

            // The callback for receiving the raw H264 video data for camera live view.
            mReceivedVideoDataCallBack = new DJICamera.CameraReceivedVideoDataCallback() {

                @Override
                public void onResult(byte[] videoBuffer, int size) {
                    if (mCodecManager != null) {

                        // Send the raw H264 video data to codec manager for decoding
                        mCodecManager.sendDataToDecoder(videoBuffer, size);
                    } else {
                        Log.e(LOG_TAG, "initVideoProcessing(): mCodecManager is null.");
                    }
                }
            };

            // The callback for receiving the raw video data from Airlink.
            mOnReceivedVideoCallback = new DJILBAirLink.DJIOnReceivedVideoCallback() {

                @Override
                public void onResult(byte[] videoBuffer, int size) {

                    // Send the raw H264 video data to codec manager for decoding.
                    if (mCodecManager != null) {
                        mCodecManager.sendDataToDecoder(videoBuffer, size);
                    }
                }
            };
        }
    }

    /** THREAD METHODS _________________________________________________________________________ **/

    public void startImageProcessingThread(boolean isStart) {
        if (isStart) {
            Log.d(LOG_TAG, "startImageProcessingThread(): Image processing thread started.");
            dataImageHandler.postDelayed(dataProcessThread, 1000); // Begins thread callbacks.
        } else {
            Log.d(LOG_TAG, "startImageProcessingThread(): Image processing thread stopped.");
            currentBitmapFrame = null; // Sets bitmap frame object to null.
            dataImageHandler.removeCallbacks(dataProcessThread); // Removes thread callbacks.
        }
    }

    private Runnable dataProcessThread = new Runnable() {

        public void run() {

            Log.d(LOG_TAG, "dataProcessThread(): Data process thread running...");

            currentBitmapFrame = videoTextureView.getBitmap(); // Gets the current bitmap from the TextureView.
            currentBitmapFrame = ImageHelper.rotateBitmap(currentBitmapFrame, 90); // Rotates the bitmap by 90 degrees.

            //testImageView.setImageBitmap(currentBitmapFrame);

            // If currentBitmapFrame is not null, the bitmap is processed.
            if (currentBitmapFrame != null) {
                Log.d(LOG_TAG, "dataProcessThread(): Bitmap image found. Processing image...");
                EmotionApiUtil.processImageForEmotions(currentBitmapFrame, client, MainActivity.this);

                // TODO: Sending fake Face data to server.
//                Random randomizer = new Random(999);
//                Face fakeFaceData = new Face(randomizer.nextInt(1000), 0.10f / randomizer.nextFloat(), 0.20f / randomizer.nextFloat(), 0.30f / randomizer.nextFloat(), 0.10f / randomizer.nextFloat(), 0.10f / randomizer.nextFloat(), 0.10f / randomizer.nextFloat(), 0.10f / randomizer.nextFloat(), 0.10f / randomizer.nextFloat());
//                ApiUtility.sendEmotionAnalytics(fakeFaceData);
            }

            dataImageHandler.postDelayed(this, INTERVAL_CHECK); // Thread is run again in 10000 ms.
        }
    };

    /** BROADCAST RECEIVER METHODS _____________________________________________________________ **/

    // initBroadcastReceiver(): Register the broadcast receiver for receiving the device connection's changes.
    private void initBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(EmotilizeApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: Do something here when event occurs.
        }
    };

    /** INTERFACE METHODS ______________________________________________________________________ **/

    // deviceConnected(): This method is run after the device has been successfully connected.
    @Override
    public void deviceConnected(DJIBaseProduct product, DJICamera camera) {

        Log.d(LOG_TAG, "deviceConnected(): Device has been connected.");

        this.djiProduct = product;
        this.djiCamera = camera;

        if (null != videoTextureView) {
            videoTextureView.setSurfaceTextureListener(this);
        }

        startImageProcessingThread(true); // Starts the image processing thread.
    }

    // emotionResults(): Retrieves face emotion results.
    @Override
    public void emotionResults(Face newFace) {

        Log.d(LOG_TAG, "emotionResults(): New Face Anger Average: " + newFace.getAnger());
        Log.d(LOG_TAG, "emotionResults(): New Face Contempt Average: " + newFace.getContempt());
        Log.d(LOG_TAG, "emotionResults(): New Face Disgust Average: " + newFace.getDisgust());
        Log.d(LOG_TAG, "emotionResults(): New Face Fear Average: " + newFace.getFear());
        Log.d(LOG_TAG, "emotionResults(): New Face Happiness Average: " + newFace.getHappiness());
        Log.d(LOG_TAG, "emotionResults(): New Face Neutral Average: " + newFace.getNeutral());
        Log.d(LOG_TAG, "emotionResults(): New Face Sadness Average: " + newFace.getSadness());
        Log.d(LOG_TAG, "emotionResults(): New Face Surprise Average: " + newFace.getSurprise());

//        ToastUtil.toastyPopUp("emotionResults(): New Face Anger Average: " + newFace.getAnger(), this);
//        ToastUtil.toastyPopUp("emotionResults(): New Face Contempt Average: " + newFace.getContempt(), this);
//        ToastUtil.toastyPopUp("emotionResults(): New Face Disgust Average: " + newFace.getDisgust(), this);
//        ToastUtil.toastyPopUp("emotionResults(): New Face Fear Average: " + newFace.getFear(), this);
//        ToastUtil.toastyPopUp("emotionResults(): New Face Happiness Average: " + newFace.getHappiness(), this);
//        ToastUtil.toastyPopUp("emotionResults(): New Face Neutral Average: " + newFace.getNeutral(), this);
//        ToastUtil.toastyPopUp("emotionResults(): New Face Sadness Average: " + newFace.getSadness(), this);
//        ToastUtil.toastyPopUp("emotionResults(): New Face Surprise Average: " + newFace.getSurprise(), this);

        // Updates the fragment.
        if (currentFragment != null) {
            currentFragment.onUpdateFaceData(newFace);
        }

        faceList.add(newFace); // Adds a new Face to the list.
        ApiUtility.sendEmotionAnalytics(newFace); // Sends face data to the server.
        currentBitmapFrame = null; // Sets bitmap frame object to null.
    }
}