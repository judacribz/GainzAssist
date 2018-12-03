package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.anton46.stepsview.StepsView;

import static ca.judacribz.gainzassist.activities.start_workout.CurrWorkout.MIN_REPS;
import static ca.judacribz.gainzassist.util.Preferences.*;

public class WorkoutScreen extends Fragment implements CurrWorkout.TimerListener {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_HOW_TO_VID = "ca.judacribz.gainzassist.EXTRA_HOW_TO_VID";

    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    Bundle bundle;
    EquipmentView equipmentView;
    CountDownTimer countDownTimer;
    long currTime;
    CurrWorkout currWorkout = CurrWorkout.getInstance();

    ArrayList<Exercise> finExercises;

    // --------------------------------------------------------------------------------------------
    // UI Elements
    @BindView(R.id.rl_equip_disp) ViewGroup vgEquipDisp;
    @BindView(R.id.tv_timer) TextView tvTimer;

    @BindView(R.id.tv_exercise_title) TextView tvExerciseTitle;
    @BindView(R.id.tv_set_info) TextView tvSetInfo;
    @BindView(R.id.tv_ex_info) TextView tvExInfo;

    @BindView(R.id.btn_how_to) Button btnHowTo;
    @BindView(R.id.btn_dec_reps) ImageButton btnDecReps;
    @BindView(R.id.btn_dec_weight) ImageButton btnDecWeight;

    @BindView(R.id.et_curr_reps) EditText etCurrReps;
    @BindView(R.id.et_curr_weight) EditText etCurrWeight;
//    @BindView(R.id.svExs) StepsView svExs;
//    @BindView(R.id.svSets) StepsView svSets;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WorkoutScreen Constructor/Instance                                                          //
    // ######################################################################################### //
    public WorkoutScreen() {
        // Required empty public constructor
    }

    public static WorkoutScreen newInstance() {
        return new WorkoutScreen();
    }
    // ######################################################################################### //

    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (StartWorkout) context;
        currWorkout.setContext(act);

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

        currWorkout.setTimerListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupEquipView();
//        setProgress(svExs, exStrs, currWorkout.getCurrNumExs(), currWorkout.getCurrExNum());
//        setProgress(svSets, setStrs, currWorkout.getCurrNumSets(), currWorkout.getCurrSetNum());
    }

//    String[] setStrs, exStrs;
//    void setProgress(StepsView sv, String[] labels, int progLen, int pos) {
//        if (labels == null) {
//            labels = new String[progLen];
//            for (int i = 0; i < progLen; i++) {
//                labels[i] = String.valueOf(i + 1);
//            }
//        } else if(labels.length != progLen) {
//
//            labels = new String[progLen];
//            for (int i = 0; i < progLen; i++) {
//                labels[i] = String.valueOf(i + 1);
//            }
//        }
//        sv.setLabels(labels)
//                .setBarColorIndicator(getContext().getResources().getColor(R.color.material_blue_grey_800))
//                .setProgressColorIndicator(getContext().getResources().getColor(R.color.colorGreen))
//                .setLabelColorIndicator(getContext().getResources().getColor(R.color.colorGreen))
//                .setCompletedPosition(pos-1)
//                .drawView();
//    }

    // ExerciseSet up the custom view (EquipmentView) to display the equipment. View added dynamically
    // to trigger onDraw method
    public void setupEquipView() {
        equipmentView = new EquipmentView(act);
        int p  = getResources().getDisplayMetrics().widthPixels;


        equipmentView.setLayoutParams(new LayoutParams(
                        p/2,
                        LayoutParams.MATCH_PARENT
        ));


        vgEquipDisp.addView(equipmentView, 0);
        tvTimer.setWidth(p/2);
        updateUI();
    }

    @Override
    public void startTimer(long timeInMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
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

                tvTimer.setText((minutes + ":" + String.format(Locale.getDefault(), "%02d", seconds)));
            }

            // Changes the timer text, when it gets to 0:00, to "Start the next set"
            public void onFinish() {
                tvTimer.setText(R.string.start_next_set);
                cancel();
            }
        };
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////

    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(value = R.id.et_curr_reps, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeRepsChanged() {
        if (!btnDecReps.isEnabled())
            btnDecReps.setEnabled(true);
    }

    @OnTextChanged(value = R.id.et_curr_reps, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onRepsChanged(CharSequence s, int start, int before, int count) {
        String repStr = s.toString();
        int reps;
        if (!repStr.isEmpty())
            reps = Integer.valueOf(repStr);
        else
            reps = MIN_REPS;

        if (currWorkout.setCurrReps(reps))
            btnDecReps.setEnabled(false);
    }

    // TextWatcher for weight ET
    @OnTextChanged(
            value = R.id.et_curr_weight,
            callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeWeightChanged() {
        if (!btnDecWeight.isEnabled())
            btnDecWeight.setEnabled(true);
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

        if (currWorkout.setWeight(weight))
            btnDecWeight.setEnabled(false);
    }
    // =TextWatcher=Handling========================================================================

    // Click Handling
    // =============================================================================================
    @OnClick(R.id.btn_how_to)
    public void startWorkoutsList() {
        Intent intent = new Intent(act, HowToVideos.class);
        intent.putExtra(EXTRA_HOW_TO_VID, currWorkout.getCurrExName());
        startActivity(intent);
    }

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
        } else {
            if (removeIncompleteWorkoutPref(act, currWorkout.getWorkoutName())) {
                removeIncompleteSessionPref(act, currWorkout.getWorkoutName());
            }

            act.finish();
        }
    }

    public void updateUI() {
        String setType;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (currWorkout.getIsWarmup()) {
            setType = "Warmup";
            tvTimer.setText(R.string.start_next_set);
        } else {
            setType = "Main";
        }

        setReps();
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

//        setProgress(svExs, exStrs, currWorkout.getCurrNumExs(), currWorkout.getCurrExNum());
//        setProgress(svSets, setStrs, currWorkout.getCurrNumSets(), currWorkout.getCurrSetNum());


        tvExerciseTitle.setText(currWorkout.getCurrExName());

    }

    private void setReps() {
        etCurrReps.setText(String.valueOf(currWorkout.getCurrReps()));
    }
    float weight;
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
