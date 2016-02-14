package osmo.devweekhack2016.com.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import osmo.devweekhack2016.com.R;
import osmo.devweekhack2016.com.data.MathUtil;
import osmo.devweekhack2016.com.interfaces.OnEmotionResultsUpdated;
import osmo.devweekhack2016.com.ui.ToastUtil;

/**
 * Created by Michael Yoon Huh on 2/13/2016.
 */
public class EmotionApiUtil {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    private static final String LOG_TAG = EmotionApiUtil.class.getSimpleName();

    /** EMOTION PROCESSING METHODS _____________________________________________________________ **/

    // processImageForEmotions(): Processes bitmap images to retrieve the emotion data result..
    public static void processImageForEmotions(Bitmap bitmap, EmotionServiceClient client, AppCompatActivity activity) {

        // Do emotion detection using auto-detected faces.
        try {
            new doRequest(bitmap, client, activity).execute();
        } catch (Exception e) {
            ToastUtil.toastyPopUp("ERROR: " + e.getMessage(), activity);
            Log.e(LOG_TAG, "processImageForEmotions(): ERROR: " + e.getMessage());
        }
    }

    private static List<RecognizeResult> processWithAutoFaceDetection(Bitmap bitmap, EmotionServiceClient client) throws EmotionServiceException, IOException {
        Log.d(LOG_TAG, "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d(LOG_TAG, String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    private static List<RecognizeResult> processWithFaceRectangles(Bitmap bitmap, EmotionServiceClient client, AppCompatActivity activity) throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d(LOG_TAG, "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d(LOG_TAG, "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = activity.getString(R.string.face_api_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d(LOG_TAG, String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d(LOG_TAG, "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d(LOG_TAG, String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    /** SUBCLASSES _____________________________________________________________________________ **/

    private static class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {

        private Bitmap bitmap;
        private EmotionServiceClient client;

        private AppCompatActivity activity;
        private Exception e = null; // Store error message.
        private boolean useFaceRectangles = true; // Uses the Face rectangle parameters.

        public doRequest(Bitmap bitmap, EmotionServiceClient client, AppCompatActivity activity) {
            this.bitmap = bitmap;
            this.client = client;
            this.activity = activity;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection(bitmap, client);
                } catch (Exception e) {
                    this.e = e; // Stores error.
                    ToastUtil.toastyPopUp("ERROR: " + e.getMessage(), activity);
                    Log.e(LOG_TAG, "doRequest(): ERROR: " + e.getMessage());
                }
            } else {
                try {
                    return processWithFaceRectangles(bitmap, client, activity);
                } catch (Exception e) {
                    this.e = e; // Stores error.
                    ToastUtil.toastyPopUp("ERROR: " + e.getMessage(), activity);
                    Log.e(LOG_TAG, "doRequest(): ERROR: " + e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);

            if (result.size() == 0) {
                ToastUtil.toastyPopUp("ERROR: No emotion detected.", activity);
                Log.e(LOG_TAG, "doRequest(): ERROR: No emotion detected.");
            } else {

                Log.d(LOG_TAG, "doRequest(): " + result.size() + " faces detected in image.");

                // TODO: Draws a rectangle around the recognized faces.
//                Integer count = 0;
//                Canvas faceCanvas = new Canvas(bitmap);
//                faceCanvas.drawBitmap(bitmap, 0, 0, null);
//                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(5);
//                paint.setColor(Color.RED);

                // Calculates the face emotion averages of the result and updates the attached
                // activity.
                updateFace(MathUtil.calculateEmotionAverages(result));
            }
        }

        // updateFace(): Sends the new face data to the attached activity.
        private void updateFace(osmo.devweekhack2016.com.model.Face newFace) {
            try { ((OnEmotionResultsUpdated) activity).emotionResults(newFace); }
            catch (ClassCastException cce) {} // Catch for class cast exception errors.
        }
    }
}
