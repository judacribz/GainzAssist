package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos;
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.activities.start_workout.CurrWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;
import com.orhanobut.logger.Logger;


import static ca.judacribz.gainzassist.util.Preferences.*;
import static ca.judacribz.gainzassist.util.UI.handleFocusLeft;
import static ca.judacribz.gainzassist.constants.ExerciseConst.*;

public class WorkoutScreen extends Fragment implements CurrWorkout.TimerListener {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    Bundle bundle;
    CountDownTimer countDownTimer;

    long currTime;
    CurrWorkout currWorkout = CurrWorkout.getInstance();
    ArrayList<Exercise> finExercises;
    float weight;
    // --------------------------------------------------------------------------------------------

    // UI Elements
    @BindView(R.id.equip_view) EquipmentView equipmentView;
    @BindView(R.id.tv_timer) TextView tvTimer;

    @BindView(R.id.tv_exercise_title) TextView tvExerciseTitle;
    @BindView(R.id.tv_set_info) TextView tvSetInfo;
    @BindView(R.id.tv_ex_info) TextView tvExInfo;

    @BindView(R.id.btn_dec_reps) ImageButton btnDecReps;
    @BindView(R.id.btn_dec_weight) ImageButton btnDecWeight;

    @BindView(R.id.et_curr_reps) EditText etCurrReps;
    @BindView(R.id.et_curr_weight) EditText etCurrWeight;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WorkoutScreen Constructor/Instance                                                        //
    // ######################################################################################### //
    public WorkoutScreen() {
    }

    public static WorkoutScreen getInstance() {
        return new WorkoutScreen();
    }
    // ######################################################################################### //

    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (StartWorkout) context;

        // init finished workout variables
        finExercises = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workout_screen, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        currWorkout.setTimerListener(this);

        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();

        currWorkout.setTimerListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////

    @Override
    public void startTimer(long timeInMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            Logger.d("CANCEL TIME");
        }

        countDownTimer = getCountDownTimer(timeInMillis);
        countDownTimer.start();
    }

    /* Creates and returns a new CountDownTimer with the rest time to count down from. Has the
     * following format: 0:00
     */
    CountDownTimer getCountDownTimer(long milliseconds) {

        return new CountDownTimer(milliseconds, 1000) {
            long seconds;
            long minutes;

            // Calculates the minutes and seconds to display in 0:00 format
            public void onTick(long millisUntilFinished) {
                currTime = millisUntilFinished;
                seconds = currTime / 1000;
                minutes = seconds / 60;
                seconds = seconds % 60;

                String time = minutes + ":" + String.format(Locale.getDefault(), "%02d", seconds);
                tvTimer.setText(time);
            }

            // Changes the timer text, when it gets to 0:00, to "Start the next set"
            public void onFinish() {
                tvTimer.setText(R.string.start_next_set);
                cancel();
            }
        };
    }

    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(value = R.id.et_curr_reps, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeRepsChanged() {
        if (!btnDecReps.isEnabled()) {
            btnDecReps.setEnabled(true);
            btnDecReps.setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(value = R.id.et_curr_reps, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onRepsChanged(CharSequence s, int start, int before, int count) {
        String repStr = s.toString();
        int reps;
        if (!repStr.isEmpty())
            reps = Integer.valueOf(repStr);
        else
            reps = MIN_REPS;

        currWorkout.setCurrReps(reps, false);

        if (currWorkout.isMinReps()) {
            btnDecReps.setEnabled(false);
            btnDecReps.setVisibility(View.INVISIBLE);
        }
    }

    // TextWatcher for weight ET
    @OnTextChanged(
            value = R.id.et_curr_weight,
            callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeWeightChanged() {
        if (!btnDecWeight.isEnabled()) {
            btnDecWeight.setEnabled(true);
            btnDecWeight.setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(value = R.id.et_curr_weight, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onWeightChanged(CharSequence s, int start, int before, int count) {
        float weight;
        if (!s.toString().isEmpty()) {
            weight = Float.valueOf(s.toString());
            if (equipmentView != null)
                equipmentView.setup(weight, currWorkout.getCurrEquip());
        } else
            weight = currWorkout.getCurrMinWeight();

        currWorkout.setWeight(weight);

        if (currWorkout.isMinWeight() || weight <= currWorkout.getCurrMinWeight()) {
            btnDecWeight.setEnabled(false);
            btnDecWeight.setVisibility(View.INVISIBLE);
        }
    }
    // =TextWatcher=Handling========================================================================

    // OnFocusChanged Handling
    // =============================================================================================
    @OnFocusChange({R.id.et_curr_reps, R.id.et_curr_weight})
    void onFocusLeft(EditText et, boolean hasFocus) {
        if (!hasFocus) {
            Number min, res;
            switch (et.getId()) {
                case R.id.et_curr_weight:
                    min = currWorkout.getCurrMinWeight();
                    res = currWorkout.getCurrWeight();
                    break;
                default:
                    min = MIN_REPS;
                    res = currWorkout.getCurrReps();
                    break;
            }

            handleFocusLeft(et, min, res);
         }
    }
    // =OnFocusChanged=Handling=====================================================================


    // Click Handling
    // =============================================================================================
    @OnClick(R.id.tv_timer)
    public void changeTimerState() {
        if (currTime / 1000 != 0) {
            if (countDownTimer == null) {
                startTimer(currTime);
            } else {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }
    }

    // Reps change
    @OnClick({R.id.btn_inc_reps, R.id.btn_dec_reps})
    public void changeReps(ImageButton repsBtn) {
        switch (repsBtn.getId()) {
            case R.id.btn_inc_reps:
                currWorkout.incReps();
                break;
            case R.id.btn_dec_reps:
                currWorkout.decReps();
                break;
        }
        setReps();
    }

    // Weight change
    @OnClick({R.id.btn_inc_weight, R.id.btn_dec_weight})
    public void changeWeight(ImageButton weightBtn) {
        switch (weightBtn.getId()) {
            case R.id.btn_inc_weight:
                currWorkout.incWeight();
                break;
            case R.id.btn_dec_weight:
                currWorkout.decWeight();
                break;
        }
        setWeight();
    }

    @OnClick(R.id.btn_finish_set)
    public void finishSet() {
        if (currWorkout.finishCurrSet()) {

            updateUI();

        // End of workout
        } else {
            if (removeIncompleteWorkoutPref(act, currWorkout.getWorkoutName())) {
                removeIncompleteSessionPref(act, currWorkout.getWorkoutName());
            }

            ViewModelProviders.of(act)
                    .get(WorkoutViewModel.class)
                    .insertSession(currWorkout.getCurrSession());
            if (removeIncompleteWorkoutPref(act, currWorkout.getWorkoutName())) {
                removeIncompleteSessionPref(act, currWorkout.getWorkoutName());
            }

            act.finish();
        }
    }

    public void updateUI() {
        String setType;

        if (currWorkout.getIsWarmup()) {
            if (countDownTimer != null) {
                countDownTimer.onFinish();
                countDownTimer = null;
            }
            setType = "Warmup";
        } else {
            setType = "Main";
        }

        if (!currWorkout.getLockReps())
            setReps();

        if (!currWorkout.getLockWeight())
            setWeight();

        tvExInfo.setText(String.format(
                "%s %s/%s",
                setType,
                currWorkout.getCurrExNum(),
                currWorkout.getCurrNumExs())
        );

        tvSetInfo.setText(String.format(
                "Set %s/%s",
                currWorkout.getCurrSetNum(),
                currWorkout.getCurrNumSets())
        );

        tvExerciseTitle.setText(currWorkout.getCurrExName());
    }

    private void setReps() {
        etCurrReps.setText(String.valueOf(currWorkout.getCurrReps()));
    }

    public void setWeight() {
        weight = currWorkout.getCurrWeight();
        etCurrWeight.setText(String.valueOf(weight));

        equipmentView.post(new Runnable() {
            @Override
            public void run() {
                equipmentView.setup(weight, currWorkout.getCurrEquip());
            }
        });
    }
    //=Click=Handling===============================================================================
}
