package osmo.devweekhack2016.com.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import osmo.devweekhack2016.com.R;
import osmo.devweekhack2016.com.activities.MainActivity;
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
    private HorizontalBarChart barChart;

    private static final String DATE_FORMAT = "EEEE MMMM d, yyyy 'at' h:mm aaa";
    private static final float DEFAULT_BAR_VALUE = 0.0f;

    private Activity currentActivity;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        return new MainFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.currentActivity = activity;
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

        // Sets the current fragment.
        ((MainActivity) currentActivity).setFragment(this);

        // create a new chart object
        barChart = (HorizontalBarChart) view.findViewById(R.id.chart);

        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDescription(null);
        barChart.setTouchEnabled(false);
        barChart.getLegend().setEnabled(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinValue(0f);

        barChart.getAxisRight().setEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setEnabled(true);

        barChart.invalidate();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setInitialChartBarData();

        //TODO remove the method call below. It should be called when face data is available.
        onUpdateFaceData(getDummyData());
    }

    private void setInitialChartBarData() {
        Resources res = getResources();
        String[] emotions = res.getStringArray(R.array.emotions_array);
        ArrayList<String> xLabels = new ArrayList<String>();

        for (int i = 0; i < emotions.length; i++) {
            xLabels.add(emotions[i]);
        }

        BarData barChartData = new BarData(xLabels,
                createBarChartDataSet(getInitialBarEntryValues()));
        barChart.setData(barChartData);
    }

    //TODO remove this method
    private Face getDummyData() {
        Face dummy = new Face();
        dummy.setAnger(0f);
        dummy.setContempt(0f);
        dummy.setDisgust(0f);
        dummy.setFear(0f);
        dummy.setHappiness(0f);
        dummy.setNeutral(0f);
        dummy.setSadness(0f);
        dummy.setSurprise(0f);
        dummy.setDate(new Date());
        return dummy;
    }

    public void onUpdateFaceData(Face face) {
        faceArrayList.add(face);
        addFaceDataToTextViews(face);
        addFaceDataToBarChart(face);
    }

    private void addFaceDataToBarChart(Face face) {
        BarData allBarData = barChart.getData();
        ArrayList<BarEntry> barEntries = getBarEntriesFromFace(face);

        if(allBarData != null) {

            IBarDataSet oldBarData = allBarData.getDataSetByIndex(0);
            allBarData.removeDataSet(oldBarData);

            IBarDataSet newBarData = createBarChartDataSet(barEntries);
            allBarData.addDataSet(newBarData);

            // let the chart know it's barData has changed
            barChart.notifyDataSetChanged();

            barChart.setVisibleXRangeMaximum(8);
            barChart.setVisibleYRangeMaximum(15, YAxis.AxisDependency.LEFT);

            // this automatically refreshes the chart (calls invalidate())
            barChart.moveViewTo(allBarData.getXValCount()-7, 50f, YAxis.AxisDependency.LEFT);

        }
    }

    private ArrayList<BarEntry> getBarEntriesFromFace(Face face) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        barEntries.add(new BarEntry(face.getAnger(), 7, getString(R.string.anger)));
        barEntries.add(new BarEntry(face.getContempt(), 6, getString(R.string.contempt)));
        barEntries.add(new BarEntry(face.getDisgust(), 5, getString(R.string.disgust)));
        barEntries.add(new BarEntry(face.getFear(), 4, getString(R.string.fear)));
        barEntries.add(new BarEntry(face.getHappiness(), 3,getString(R.string.happiness)));
        barEntries.add(new BarEntry(face.getNeutral(), 2, getString(R.string.neutral)));
        barEntries.add(new BarEntry(face.getSadness(), 1, getString(R.string.sadness)));
        barEntries.add(new BarEntry(face.getSurprise(), 0, getString(R.string.surprise)));

        return barEntries;
    }
    private double convertFloatToPercent(float f) {
        return (f * 100);
    }


    private BarDataSet createBarChartDataSet(List<BarEntry> yVals) {

        BarDataSet set = new BarDataSet(yVals, getString(R.string.bar_chart_title));
        set.setBarSpacePercent(10f);

        int[] colors = {
            Color.rgb(83, 198, 83),
            Color.rgb(51, 102, 255),
            Color.rgb(102, 153, 153),
            Color.rgb(255, 255, 0),
            Color.rgb(102, 0, 204),
            Color.rgb(102, 51, 0),
            Color.rgb(255, 102, 0),
            Color.rgb(255, 0, 0)

        };
        set.setColors(colors);
        set.setBarShadowColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setValueTextSize(10f);

        return set;
    }

    private ArrayList<BarEntry> getInitialBarEntryValues() {
        ArrayList<BarEntry> barEntries = new ArrayList<>(8);

        for (int i = 0; i < barEntries.size(); i++) {
            barEntries.add(new BarEntry(DEFAULT_BAR_VALUE, i));
        }

        return barEntries;
    }

    private void addFaceDataToTextViews(Face face) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);

        if (face.getDate() != null) {
            titleText.setText(String.format(getString(R.string.main_fragment_title), sdf.format(face
                    .getDate
                            ())));
        }

        angerText.setText(String.format(getString(R.string.anger), convertFloatToPercent(face
                .getAnger())));
        contemptText.setText(String.format(getString(R.string.contempt), convertFloatToPercent(face
                .getContempt())));
        disgustText.setText(String.format(getString(R.string.disgust), convertFloatToPercent(face
                .getDisgust())));
        fearText.setText(String.format(getString(R.string.fear), convertFloatToPercent(face
                .getFear())));
        happinessText.setText(String.format(getString(R.string.happiness), convertFloatToPercent
                (face.getHappiness())));
        neutralText.setText(String.format(getString(R.string.neutral), convertFloatToPercent(face
                .getNeutral())));
        sadnessText.setText(String.format(getString(R.string.sadness), convertFloatToPercent(face
                .getSadness())));
        surpriseText.setText(String.format(getString(R.string.surprise), convertFloatToPercent(face
                .getSurprise())));

    }
}
