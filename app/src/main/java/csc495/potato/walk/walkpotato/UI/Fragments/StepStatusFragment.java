package csc495.potato.walk.walkpotato.UI.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import csc495.potato.walk.walkpotato.R;
import csc495.potato.walk.walkpotato.UI.fitlib.History;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StepStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StepStatusFragment extends Fragment {
    private static int stepsTakenToday = 0;

    private static int entertainmentTime = 600;
    private static int usedUpTime = 0;
    public static int MAX_ENTERTAINMENT_TIME = 600;
    private final int NUM_STEPS_PER_CAL = 20;
    private final int NUM_CALS_PER_POTATO = 138;
    private PieChart mPieChart;
    private TextView stepsView;
    private TextView totalPotatoesView;
    private int goal;
    //private Context context;
    private PieModel sliceGoal, sliceCurrent;

    public StepStatusFragment() {
        // Required empty public constructor
        //this.context = getActivity().getApplicationContext();

    }

    private BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Total Steps service", "Insert something here");
            if (intent.hasExtra(History.HISTORY_EXTRA_STEPS_TODAY)) {

                stepsTakenToday = intent.getIntExtra(History.HISTORY_EXTRA_STEPS_TODAY, 0);
                Log.d("Total Steps", "" + stepsTakenToday);
                updatePie();
            }
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StepStatusFragment.
     */
    public static StepStatusFragment newInstance() {
        StepStatusFragment fragment = new StepStatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;

    }

    public static int getStepsTakenToday() {
        return stepsTakenToday;
    }

    public static void setStepsTakenToday(int stepsTakenToday) {
        StepStatusFragment.stepsTakenToday = stepsTakenToday;
    }

    public int getGoalSteps() {
        SharedPreferences prefs =
                getActivity().getSharedPreferences("walkpotato", Context.MODE_MULTI_PROCESS);
        return prefs.getInt("goal", SettingsFragment.DEFAULT_GOAL);
    }

    public static int getEntertainmentTime() {
        return entertainmentTime;
    }

    public static void setEntertainmentTime() {
        entertainmentTime = MAX_ENTERTAINMENT_TIME - usedUpTime;
    }

    public static void clearEntertainmentTime() {
        entertainmentTime = 0;
    }

    public static void resetTimer() {
        entertainmentTime = MAX_ENTERTAINMENT_TIME;
    }

    public static int getUsedUpTime() {
        return usedUpTime;
    }

    public static void setUsedUpTime(int time) {
        usedUpTime += time;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(stepUpdateReceiver, new IntentFilter(History.HISTORY_INTENT));
        Intent service = new Intent(getActivity(), History.class);
        service.putExtra(History.SERVICE_REQUEST_TYPE, History.TYPE_GET_STEP_TODAY_DATA);
        getActivity().startService(service);
        super.onCreate(savedInstanceState);

    }

    private void handleGetStepsButton() {
        //Start Service and wait for broadcast

    }

    private void updatePie() {
        //if (BuildConfig.DEBUG) Logger.log("UI - update steps: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        //int steps_today = Math.max(todayOffset + since_boot, 0);
        sliceCurrent.setValue(stepsTakenToday);
        if (getGoalSteps() - stepsTakenToday > 0) {
            // goal not reached yet
            if (mPieChart.getData().size() == 1) {
                // can happen if the goal value was changed: old goal value was
                // reached but now there are some steps missing for the new goal
                mPieChart.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(getGoalSteps() - stepsTakenToday);
        } else {
            // goal reached
            mPieChart.clearChart();
            mPieChart.addPieSlice(sliceCurrent);
        }
        mPieChart.update();
        stepsView.setText(Integer.toString(stepsTakenToday));
        int numPotatoesBurned = calculatePotatoesBurned(stepsTakenToday);
        //((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));
        totalPotatoesView.setText(numPotatoesBurned == 1 ? numPotatoesBurned + " potato"
                : numPotatoesBurned + " potatoes");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_step_status, container, false);

        //int stepsTaken = 15000;
        //int goalSteps = 20000;
        int numPotatoesBurned = calculatePotatoesBurned(stepsTakenToday);
        //handleGetStepsButton();
        mPieChart = (PieChart) v.findViewById(R.id.graph);
        stepsView = (TextView) v.findViewById(R.id.steps);
        totalPotatoesView = (TextView) v.findViewById(R.id.potatoes);
        Log.d("Steps taken today are: ", "" + stepsTakenToday);
        stepsView.setText(Integer.toString(stepsTakenToday));
        totalPotatoesView.setText(numPotatoesBurned == 1 ? numPotatoesBurned + " potato"
                : numPotatoesBurned + " potatoes");

        mPieChart.clearChart();
        sliceCurrent = new PieModel("", stepsTakenToday, Color.parseColor("#F38630"));
        mPieChart.addPieSlice(sliceCurrent);
        sliceGoal = new PieModel("", getGoalSteps() - stepsTakenToday, Color.parseColor("#E0E4CC"));
        mPieChart.addPieSlice(sliceGoal);

        mPieChart.startAnimation();

        ((TextView) v.findViewById(R.id.unit)).setText(getString(R.string.steps));

        return v;

    }

    @Override
    public void onResume() {
//        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(stepUpdateReceiver, new IntentFilter(History.HISTORY_INTENT));
//        Intent service = new Intent(getActivity(), History.class);
//        service.putExtra(History.SERVICE_REQUEST_TYPE, History.TYPE_GET_STEP_TODAY_DATA);
//        getActivity().startService(service);

        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(stepUpdateReceiver);
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private int calculatePotatoesBurned(int stepsTaken) {
        return (stepsTaken / NUM_STEPS_PER_CAL) / NUM_CALS_PER_POTATO;
    }
}
