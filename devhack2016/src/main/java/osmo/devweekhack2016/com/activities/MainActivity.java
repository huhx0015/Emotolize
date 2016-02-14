package osmo.devweekhack2016.com.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import java.util.LinkedList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import dji.sdk.AirLink.DJILBAirLink;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Codec.DJICodecManager;
import dji.sdk.base.DJIBaseProduct;
import osmo.devweekhack2016.com.R;
import osmo.devweekhack2016.com.application.EmotilizeApplication;
import osmo.devweekhack2016.com.image.EmotionApiUtil;
import osmo.devweekhack2016.com.image.OsmoUtil;
import osmo.devweekhack2016.com.interfaces.OnDeviceConnected;
import osmo.devweekhack2016.com.interfaces.OnEmotionResultsUpdated;
import osmo.devweekhack2016.com.model.Face;
import osmo.devweekhack2016.com.fragments.MainFragment;
import osmo.devweekhack2016.com.ui.ToastUtil;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, OnDeviceConnected, OnEmotionResultsUpdated {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static int INTERVAL_CHECK = 30000; // Sets interval to 30 seconds.

    private Bitmap currentBitmapFrame = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888); // Stores the current Bitmap frame.
    private EmotionServiceClient client;
    private Handler dataImageHandler = new Handler(); // Handler for the data thread.
    private DJIBaseProduct djiProduct = null;
    private DJICamera djiCamera = null;

    protected DJICodecManager mCodecManager = null; // Codec for video live view.
    protected DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;
    protected DJILBAirLink.DJIOnReceivedVideoCallback mOnReceivedVideoCallback = null;

    private List<Face> faceList = new LinkedList<>();

    @Bind(R.id.video_preview_surface) TextureView videoTextureView;

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

        OsmoUtil.initDevice(mReceivedVideoDataCallBack, mOnReceivedVideoCallback, this);

        if (videoTextureView == null) {
            Log.e(LOG_TAG, "videoTextureView is null");
        }
    }

    @Override
    public void onPause() {
        startImageProcessingThread(false); // Stops the image processing thread.
        OsmoUtil.uninitDevice(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        OsmoUtil.uninitDevice(this);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    /** OVERRIDE METHODS _______________________________________________________________________ **/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCodecManager == null) {
            Log.e(LOG_TAG, "mCodecManager is null 2");
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    /** VIDEO METHODS __________________________________________________________________________ **/

    private void initVideoProcessing() {

        if (null != videoTextureView) {
            videoTextureView.setSurfaceTextureListener(this);
        }

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new DJICamera.CameraReceivedVideoDataCallback() {

            @Override
            public void onResult(byte[] videoBuffer, int size) {
                if(mCodecManager != null){

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
                if(mCodecManager != null){
                    // Send the raw H264 video data to codec manager for decoding
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };
    }

    /** THREAD METHODS _________________________________________________________________________ **/

    public void startImageProcessingThread(boolean isStart) {
        if (isStart) {
            Log.d(LOG_TAG, "startImageProcessingThread(): Image processing thread started.");
            videoTextureView.getBitmap(currentBitmapFrame);
            //currentBitmapFrame = videoTextureView.getBitmap(); // Gets the current bitmap from the TextureView.
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

            // If currentBitmapFrame is not null, the bitmap is processed.
            if (currentBitmapFrame != null) {
                Log.d(LOG_TAG, "dataProcessThread(): Bitmap image found. Processing image...");
                EmotionApiUtil.processImageForEmotions(currentBitmapFrame, client, MainActivity.this);
            }

            dataImageHandler.postDelayed(this, INTERVAL_CHECK); // Thread is run again in 30000 ms.
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

        ToastUtil.toastyPopUp("emotionResults(): New Face Anger Average: " + newFace.getAnger(), this);
        ToastUtil.toastyPopUp("emotionResults(): New Face Contempt Average: " + newFace.getContempt(), this);
        ToastUtil.toastyPopUp("emotionResults(): New Face Disgust Average: " + newFace.getDisgust(), this);
        ToastUtil.toastyPopUp("emotionResults(): New Face Fear Average: " + newFace.getFear(), this);
        ToastUtil.toastyPopUp("emotionResults(): New Face Happiness Average: " + newFace.getHappiness(), this);
        ToastUtil.toastyPopUp("emotionResults(): New Face Neutral Average: " + newFace.getNeutral(), this);
        ToastUtil.toastyPopUp("emotionResults(): New Face Sadness Average: " + newFace.getSadness(), this);
        ToastUtil.toastyPopUp("emotionResults(): New Face Surprise Average: " + newFace.getSurprise(), this);

        faceList.add(newFace); // Adds a new Face to the list.
        currentBitmapFrame = null; // Sets bitmap frame object to null.
    }
}