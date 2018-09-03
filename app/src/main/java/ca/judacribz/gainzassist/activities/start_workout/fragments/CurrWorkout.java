package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.User;

public class CurrWorkout extends Fragment{

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    ViewGroup vgEquipment;
    EquipmentView equipmentView;

    CountDownTimer countDownTimer;
    // --------------------------------------------------------------------------------------------
    // UI Elements
    @BindView(R.id.tv_timer)
    TextView tv_timer;
    // --------------------------------------------------------------------------------------------
    // ######################################################################################### //
    // CurrWorkout Constructor/Instance                                                          //
    // ######################################################################################### //
    public CurrWorkout() {
        // Required empty public constructor
    }

    public static CurrWorkout newInstance() {
        return new CurrWorkout();
    }
    // ######################################################################################### //


    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (StartWorkout) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_curr_workout, container, false);
        ButterKnife.bind(this, view);

        // View to insert EquipmentView
        vgEquipment = (ViewGroup) view.findViewById(R.id.rlEquipmentDisplay);

        // Set up the custom view (EquipmentView) to display the equipment. View added dynamically
        // to trigger onDraw method
        equipmentView = new EquipmentView(act);
        equipmentView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        vgEquipment.addView(equipmentView, 0);

//TODO: make deterministic
        equipmentView.post(new Runnable() {
            @Override
            public void run() {
                equipmentView.setup(315, getString(R.string.barbell));
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<Exercise> warmups = User.getInstance().getWarmups();

        countDownTimer = getCountDownTimer(5000);
        countDownTimer.start();

    }

    /* Creates and returns a new CountDownTimer with the rest time to count down from. Has the
     * following format: 0:00
     */
    long currTime;
    CountDownTimer getCountDownTimer(final long milliseconds) {

        return new CountDownTimer(milliseconds, 1000) {

            String timerText = "";
            long seconds;
            long minutes;

            // Calculates the minutes and seconds to display in 0:00 format
            public void onTick(long millisUntilFinished) {
                currTime = millisUntilFinished;
                seconds = currTime / 1000;
                minutes = seconds / 60;
                seconds = seconds % 60;

                timerText = minutes + ":" + String.format(Locale.getDefault(), "%02d", seconds);
                tv_timer.setText(timerText);
            }

            // Changes the timer text, when it gets to 0:00, to "Start the next set"
            public void onFinish() {
            }
        };
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
