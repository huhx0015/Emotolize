package osmo.devweekhack2016.com.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import osmo.devweekhack2016.com.R;
import osmo.devweekhack2016.com.model.Face;


public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    @Bind(R.id.title)
    TextView titleText;
    @Bind(R.id.anger)
    TextView angerText;
    @Bind(R.id.contempt)
    TextView contemptText;
    @Bind(R.id.disgust)
    TextView disgustText;
    @Bind(R.id.fear)
    TextView fearText;
    @Bind(R.id.happiness)
    TextView happinessText;
    @Bind(R.id.neutral)
    TextView neutralText;
    @Bind(R.id.sadness)
    TextView sadnessText;
    @Bind(R.id.surprise)
    TextView surpriseText;

    private ArrayList<Face> faceArrayList;

    private static final String DATE_FORMAT = "EEEE, MMM d, yyyy 'at' h:mm aaa";


    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        faceArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        Face dummy = new Face();
        dummy.setAnger(.5f);
        dummy.setContempt(.5f);
        dummy.setDisgust(.5f);
        dummy.setFear(.5f);
        dummy.setHappiness(.5f);
        dummy.setNeutral(.5f);
        dummy.setSadness(.5f);
        dummy.setSurprise(.5f);
        dummy.setDate(new Date());

        onUpdateEmotions(dummy);

        return view;
    }

    public void onUpdateEmotions(Face face) {
        faceArrayList.add(face);


        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        titleText.setText(String.format(getString(R.string.main_fragment_title), sdf.format(face
                .getDate
                        ())));

        angerText.setText(String.format(getString(R.string.anger), converToPercent(face.getAnger
                ())));
        contemptText.setText(String.format(getString(R.string.contempt), converToPercent(face
                .getAnger())));
        disgustText.setText(String.format(getString(R.string.disgust), converToPercent(face
                .getAnger())));
        fearText.setText(String.format(getString(R.string.fear), converToPercent(face.getAnger())));
        happinessText.setText(String.format(getString(R.string.happiness), converToPercent(face
                .getAnger())));
        neutralText.setText(String.format(getString(R.string.neutral), converToPercent(face
                .getAnger())));
        sadnessText.setText(String.format(getString(R.string.sadness), converToPercent(face
                .getAnger())));
        surpriseText.setText(String.format(getString(R.string.surprise), converToPercent(face
                .getAnger())));


    }

    private double converToPercent(float f) {
        return (f * 100);
    }

}
