package com.example.lucyzhao.votingapp.nfc;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lucyzhao.votingapp.R;

import java.util.ArrayList;
import java.util.List;


public class StepperFragment extends Fragment {
    private static final String TAG = StepperFragment.class.getSimpleName();

    private List<TextView> stepperCircles = new ArrayList<>();
    private List<View> stepperLines = new ArrayList<>();

    private List<TextView> stepperLabels = new ArrayList<>();
    private int stepsCompleted = 0;

    private ProgressBar spinner;


    public StepperFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "in oncreate view");
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_stepper, container, false);

        LinearLayout ll = fragment.findViewById(R.id.stepper_circle_ll);
        Log.v(TAG, "child count is " + ll.getChildCount());
        for (int i = 0; i < ll.getChildCount(); i++) {
            View view = ll.getChildAt(i);
            if (i % 2 == 0)
                stepperCircles.add((TextView) view);
            else {
                stepperLines.add(view);
            }
        }

        LinearLayout labelsLl = fragment.findViewById(R.id.stepper_labels_ll);
        for(int i = 0 ; i < labelsLl.getChildCount(); i++) {
            stepperLabels.add((TextView) labelsLl.getChildAt(i));
        }

        return fragment;
    }

    /**
     * @param step 1-based step
     */
    public void setStepCompleted(int step) {
        stepsCompleted = step;
        Log.v(TAG, "step is " + step);
        for (int i = 0; i < step; i++) {
            TextView tv = stepperCircles.get(i);
            tv.setText("");
            tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_action_check));
        }
        for (int i = 0; i < step - 1; i++) {
            View line = stepperLines.get(i);
            line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBtnCompleted));
        }
    }

    public int getStepsCompleted() {
        return this.stepsCompleted;
    }

    public void setTaskFailed(String msg) {
        TextView tv = stepperCircles.get(stepsCompleted);
        tv.setText("");
        tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_action_err));
        if(stepsCompleted >= 1) {
            View line = stepperLines.get(stepsCompleted - 1);
            line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        }
        stepperLabels.get(stepsCompleted).setText(msg);
    }

}
