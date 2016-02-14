package osmo.devweekhack2016.com.model;

import java.util.Date;

/**
 * Created by Michael Yoon Huh on 2/13/2016.
 */
public class Face {

    private float anger;
    private float contempt;
    private float disgust;
    private float fear;
    private float happiness;
    private float neutral;
    private float sadness;
    private float surprise;
    private float[] faceRectangle;
    private Date date;

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
}
