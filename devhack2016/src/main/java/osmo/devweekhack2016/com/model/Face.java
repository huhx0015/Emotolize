package osmo.devweekhack2016.com.model;

import com.google.gson.annotations.SerializedName;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Michael Yoon Huh on 2/13/2016.
 */
public class Face {

    @SerializedName("faceId")
    private int faceId;

    @SerializedName("anger")
    private float anger;

    @SerializedName("contempt")
    private float contempt;

    @SerializedName("disgust")
    private float disgust;

    @SerializedName("fear")
    private float fear;

    @SerializedName("happiness")
    private float happiness;

    @SerializedName("neutral")
    private float neutral;

    @SerializedName("sadness")
    private float sadness;

    @SerializedName("surprise")
    private float surprise;

    @SerializedName("faceRectangle")
    private float[] faceRectangle;

    private Date date;

    /** CONSTRUCTOR METHODS ____________________________________________________________________ **/

    public Face() {}

    public Face(int id, float anger, float contempt, float disgust, float fear, float happiness,
                float neutral, float sadness, float surprise) {
        this.faceId = id;
        this.anger = anger;
        this.contempt = contempt;
        this.disgust = disgust;
        this.fear = fear;
        this.happiness = happiness;
        this.neutral = neutral;
        this.sadness = sadness;
        this.surprise = surprise;
        faceRectangle = new float[] { 0, 0, 0, 0};
    }

    /** GET / SET METHODS ______________________________________________________________________ **/

    public float getAnger() {
        return anger;
    }

    public void setAnger(float anger) {
        this.anger = anger;
    }

    public float getContempt() {
        return contempt;
    }

    public void setContempt(float contempt) {
        this.contempt = contempt;
    }

    public float getDisgust() {
        return disgust;
    }

    public void setDisgust(float disgust) {
        this.disgust = disgust;
    }

    public float getFear() {
        return fear;
    }

    public void setFear(float fear) {
        this.fear = fear;
    }

    public float getHappiness() {
        return happiness;
    }

    public void setHappiness(float happiness) {
        this.happiness = happiness;
    }

    public float getNeutral() {
        return neutral;
    }

    public void setNeutral(float neutral) {
        this.neutral = neutral;
    }

    public float getSadness() {
        return sadness;
    }

    public void setSadness(float sadness) {
        this.sadness = sadness;
    }

    public float getSurprise() {
        return surprise;
    }

    public void setSurprise(float surprise) {
        this.surprise = surprise;
    }

    public float[] getFaceRectangle() {
        return faceRectangle;
    }

    public void setFaceRectangle(float[] faceRectangle) {
        this.faceRectangle = faceRectangle;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    /** JSON METHODS ___________________________________________________________________________ **/

    public String toJSON() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("faceId", faceId);
            jsonObject.put("anger", anger);
            jsonObject.put("contempt", contempt);
            jsonObject.put("disgust", disgust);
            jsonObject.put("fear", fear);
            jsonObject.put("happiness", happiness);
            jsonObject.put("neutral", neutral);
            jsonObject.put("sadness", sadness);
            jsonObject.put("surprise", surprise);
            jsonObject.put("faceRectangle", faceRectangle);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
