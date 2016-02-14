package osmo.devweekhack2016.com.activities;

import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import java.util.LinkedList;
import java.util.List;
import osmo.devweekhack2016.com.R;
import osmo.devweekhack2016.com.application.EmotilizeApplication;
import osmo.devweekhack2016.com.image.EmotionApiUtil;
import osmo.devweekhack2016.com.interfaces.OnEmotionResultsUpdated;
import osmo.devweekhack2016.com.model.Face;
import osmo.devweekhack2016.com.fragments.MainFragment;

public class MainActivity extends AppCompatActivity implements OnEmotionResultsUpdated {

    private static int INTERVAL_CHECK = 30000; // Sets interval to 30 seconds.

    private Bitmap currentBitmapFrame; // Stores the current Bitmap frame.
    private EmotionServiceClient client;
    private Handler dataImageHandler = new Handler(); // Handler for the data thread.

    private List<Face> faceList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            MainFragment mainFragment = new MainFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, mainFragment)
                    .commit();
        }

        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.emotion_api_key));
        }

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(EmotilizeApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    public void startImageProcessingThread(boolean isStart) {
        if (isStart) {

            // TODO: Retrieve bitmap data from camera device.
            //currentBitmapFrame = OsmoUtil.fetchBitmapFromCamera();
            dataImageHandler.postDelayed(dataProcessThread, 1000); // Begins thread callbacks.
        } else {
            currentBitmapFrame = null; // Sets bitmap frame object to null.
            dataImageHandler.removeCallbacks(dataProcessThread); // Removes thread callbacks.
        }
    }

    private Runnable dataProcessThread = new Runnable() {

        public void run() {

            // If currentBitmapFrame is not null, the bitmap is processed.
            if (currentBitmapFrame != null) {
                EmotionApiUtil.processImageForEmotions(currentBitmapFrame, client, MainActivity.this);
            }

            dataImageHandler.postDelayed(this, INTERVAL_CHECK); // Thread is run again in 30000 ms.
        }
    };

    // emotionResults(): Retrieves face emotion results.
    @Override
    public void emotionResults(Face newFace) {
        faceList.add(newFace); // Adds a new Face to the list.
        currentBitmapFrame = null; // Sets bitmap frame object to null.
    }
}
