package osmo.devweekhack2016.com.data;

import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import java.util.List;
import osmo.devweekhack2016.com.model.Face;

/**
 * Created by Michael Yoon Huh on 2/13/2016.
 */
public class MathUtil {

    public static Face calculateEmotionAverages(List<RecognizeResult> resultList) {

        Face newFace = new Face();

        int numberOfFaces = resultList.size();

        float angerAverage = 0;
        float contemptAverage = 0;
        float disgustAverage = 0;
        float fearAverage = 0;
        float happinessAverage = 0;
        float neutralAverage = 0;
        float sadnessAverage = 0;
        float surpriseAverage = 0;

        // Adds the emotion values.
        for (RecognizeResult result : resultList) {
            angerAverage += result.scores.anger;
            contemptAverage += result.scores.contempt;
            disgustAverage += result.scores.disgust;
            fearAverage += result.scores.fear;
            happinessAverage += result.scores.happiness;
            neutralAverage += result.scores.neutral;
            sadnessAverage += result.scores.sadness;
            surpriseAverage += result.scores.surprise;
        }

        // Retrieves the emotion average values;
        angerAverage = angerAverage / numberOfFaces;
        contemptAverage = contemptAverage / numberOfFaces;
        disgustAverage = disgustAverage / numberOfFaces;
        fearAverage = fearAverage / numberOfFaces;
        happinessAverage = happinessAverage / numberOfFaces;
        neutralAverage = neutralAverage / numberOfFaces;
        sadnessAverage = sadnessAverage / numberOfFaces;
        surpriseAverage = surpriseAverage / numberOfFaces;

        // Sets the attributes for the Face object.
        newFace.setAnger(angerAverage);
        newFace.setContempt(contemptAverage);
        newFace.setDisgust(disgustAverage);
        newFace.setFear(fearAverage);
        newFace.setHappiness(happinessAverage);
        newFace.setNeutral(neutralAverage);
        newFace.setSadness(sadnessAverage);
        newFace.setSurprise(surpriseAverage);

        return newFace;
    }
}
