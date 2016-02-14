package osmo.devweekhack2016.com.interfaces;

import osmo.devweekhack2016.com.model.Face;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Michael Yoon Huh on 2/14/2016.
 *
 * API Documentation for Emotilize Server:
 *
 POST /api/emotion/

 A JSON end point used to send streaming of data
 headers: Common headers
 request(JSON)
 body(JSON):
 same as streaming object
 response(JSON)
 ok
 POST /api/emotion/done/

 A JSON end point used to inform the end of streaming of data
 headers: Common headers
 request(JSON)
 body(JSON):
 None
 response(JSON)
 ok
 */

public interface RetrofitInterface {

    //POST /api/emotion/
    @POST("/api/emotion/")
    Call<Face> sendFace(@Body Face face);

    //POST /api/emotion/
    @POST("/api/emotion/")
    Call<String> sendFaceString(@Body Face face);

    //POST /api/emotion/done
    @POST("/api/emotion/done")
    Call<Face> streamingDone();

}
