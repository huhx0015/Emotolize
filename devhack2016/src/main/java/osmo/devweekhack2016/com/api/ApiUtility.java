package osmo.devweekhack2016.com.api;

import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Michael Yoon Huh on 2/13/2016.
 */
public class ApiUtility {

    private static final String LOG_TAG = ApiUtility.class.getSimpleName();

    /** RETROFIT METHODS _______________________________________________________________________ **/

    private void retrieveEmotionAnalytics() {

        Retrofit retrofitAdapter = new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // TODO: Implement Retrofit request here later.

//        issuesListResult = new ArrayList<>();
//
//        RetrofitInterface apiRequest = retrofitAdapter.create(RetrofitInterface.class);
//        Call<List<Face>> call = apiRequest.getIssues(ApiConstants.BASE_URL);
//
//        call.enqueue(new Callback<List<Face>>() {
//
//            @Override
//            public void onResponse(Response<List<Issue>> response, Retrofit retrofit) {
//                if (response.isSuccess()) {
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//            }
//        });
    }
}
