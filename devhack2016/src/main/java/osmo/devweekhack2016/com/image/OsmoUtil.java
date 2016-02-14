package osmo.devweekhack2016.com.image;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import dji.sdk.AirLink.DJILBAirLink;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Camera.DJICameraSettingsDef;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;
import osmo.devweekhack2016.com.application.EmotilizeApplication;
import osmo.devweekhack2016.com.interfaces.OnDeviceConnected;
import osmo.devweekhack2016.com.ui.ToastUtil;

/**
 * Created by Michael Yoon Huh on 2/13/2016.
 */
public class OsmoUtil {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    private static final String LOG_TAG = OsmoUtil.class.getSimpleName();

    /** OSMO METHODS ___________________________________________________________________________ **/

    public static void initDevice(DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack,
                                  DJILBAirLink.DJIOnReceivedVideoCallback mOnReceivedVideoCallback,
                                  AppCompatActivity activity) {

        Log.d(LOG_TAG, "initDevice(): Device initializing...");

        DJIBaseProduct mProduct = null;
        DJICamera mCamera = null;

        try {
            mProduct = EmotilizeApplication.getProductInstance();
        } catch (Exception exception) {
            mProduct = null;
            Log.e(LOG_TAG, "initDevice(): ERROR: Device failed to initialize: " + exception.getMessage());
        }

//        if (null == mProduct || !mProduct.isConnected()) {
//            ToastUtil.toastyPopUp("OSMO Camera device is disconnected. Please connect device.", activity);
//            Log.e(LOG_TAG, "initDevice(): ERROR: OSMO Camera device is disconnected. Please connect device.");
//        } else {

            Log.d(LOG_TAG, "initDevice(): Product is connected.");

//            if (!mProduct.getModel().equals(DJIBaseProduct.Model.UnknownAircraft)) {
//                mCamera = mProduct.getCamera();
//                if (mCamera != null){
//
//                    // Sets the callback.
//                    mCamera.setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallBack);
//                }
//            } else {
//                if (null != mProduct.getAirLink()) {
//                    if (null != mProduct.getAirLink().getLBAirLink()) {
//
//                        // Set the callback.
//                        mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(mOnReceivedVideoCallback);
//                    }
//                }
//            }

            // Passes the DJIBaseProduct and DJICamera reference to the activity.
            deviceConnected(mProduct, mCamera, activity);
        //}
    }

    public static void uninitDevice(Context context) {

        DJIBaseProduct mProduct = null;
        DJICamera mCamera = null;

        try {
            mProduct = EmotilizeApplication.getProductInstance();
        } catch (Exception exception) {
            mProduct = null;
        }

        if (null == mProduct || !mProduct.isConnected()) {
            mCamera = null;
            ToastUtil.toastyPopUp("Device disconnected.", context);
        } else {
            if (!mProduct.getModel().equals(DJIBaseProduct.Model.UnknownAircraft)) {
                mCamera = mProduct.getCamera();
                if (mCamera != null){

                    // Set the callbacks.
                    mCamera.setDJICameraReceivedVideoDataCallback(null);

                }
            } else {
                if (null != mProduct.getAirLink()) {
                    if (null != mProduct.getAirLink().getLBAirLink()) {

                        // Set the callbacks.
                        mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(null);
                    }
                }
            }
        }
    }

    // function for taking photo
    private static void fetchBitmapFromCamera(DJIBaseProduct mProduct, final Context context){

        DJICameraSettingsDef.CameraMode cameraMode = DJICameraSettingsDef.CameraMode.ShootPhoto;
        final DJICamera mCamera = mProduct.getCamera();

        mCamera.setCameraMode(cameraMode, new DJIBaseComponent.DJICompletionCallback(){

            @Override
            public void onResult(DJIError error) {

                if (error == null) {
                    DJICameraSettingsDef.CameraShootPhotoMode photoMode = DJICameraSettingsDef.CameraShootPhotoMode.Single; // Set the camera capture mode as Single mode
                    mCamera.startShootPhoto(photoMode, new DJIBaseComponent.DJICompletionCallback(){

                        @Override
                        public void onResult(DJIError error) {
                            if (error == null) {
                                ToastUtil.toastyPopUp("Photo Capture SUCCESS!", context);
                            } else {
                                ToastUtil.toastyPopUp("Photo Capture FAILURE: " + error.getDescription(), context);
                            }
                        }

                    }); // Execute the startShootPhoto API
                } else {
                    ToastUtil.toastyPopUp("Photo Capture FAILURE: " + error.getDescription(), context);
                }
            }
        });
    }

    /** INTERFACE METHODS ______________________________________________________________________ **/

    // deviceConnected(): Passes the DJIBaseProduct and DJICamera reference to the activity.
    private static void deviceConnected(DJIBaseProduct product, DJICamera camera, AppCompatActivity activity) {
        try { ((OnDeviceConnected) activity).deviceConnected(product, camera); }
        catch (ClassCastException cce) {} // Catch for class cast exception errors.
    }
}
