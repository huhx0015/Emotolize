package osmo.devweekhack2016.com.api;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import osmo.devweekhack2016.com.interfaces.RetrofitInterface;
import osmo.devweekhack2016.com.model.Face;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Michael Yoon Huh on 2/13/2016.
 */
public class ApiUtility {

    private static final String LOG_TAG = ApiUtility.class.getSimpleName();

    /** RETROFIT METHODS _______________________________________________________________________ **/

    public static void sendEmotionAnalytics(Face faceData) {

        Retrofit retrofitAdapter = new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface apiRequest = retrofitAdapter.create(RetrofitInterface.class);

        Call<String> call = apiRequest.sendFaceString(faceData);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Response<String> response, Retrofit retrofit) {
                Log.d(LOG_TAG, "sendEmotionAnalytics(): Response received.");
                if (response.isSuccess()) {
                    Log.d(LOG_TAG, "sendEmotionAnalytics(): Face data transmission was successful.");
                } else {
                    Log.e(LOG_TAG, "sendEmotionAnalytics(): ERROR: " + response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "sendEmotionAnalytics(): ERROR: " + t.getMessage());
            }
        });
    }
}