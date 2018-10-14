package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos;
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.CurrSet;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.CurrUser;
import ca.judacribz.gainzassist.models.Workout;

import static android.support.v4.widget.TextViewCompat.getAutoSizeMaxTextSize;
import static android.support.v4.widget.TextViewCompat.setAutoSizeTextTypeWithDefaults;
import static android.widget.TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM;
import static ca.judacribz.gainzassist.models.CurrSet.MIN_REPS;

public class CurrWorkout extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_HOW_TO_VID = "ca.judacribz.gainzassist.EXTRA_HOW_TO_VID";

    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;

    EquipmentView equipmentView;
    CountDownTimer countDownTimer;

    CurrSet currSet = CurrSet.getInstance();
    CurrUser currUser = CurrUser.getInstance();

    Exercise currExercise;
    ArrayList<Set> sets;
    ArrayList<Exercise> warmups = null, exercises;
    int set_i = 0, ex_i = 0;

    ArrayList<Exercise> finExercises;

    boolean lockReps = false, lockWeight = false;

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
        act = (StartWorkout) context;

        // get all warmup exercises
        if (currUser.warmupsEmpty())
            currSet.setType(false);
        else {
            warmups = currUser.getWarmups();
            Toast.makeText(context, "warmup not empty", Toast.LENGTH_SHORT).show();
        }
        // get all main exercises
        exercises = StartWorkout.workout.getExercises();

        if (warmups == null || warmups.isEmpty()) {
            currExercise = exercises.get(ex_i);
        } else {
            currExercise = warmups.get(ex_i);
        }
        sets = currExercise.getSets();

        // init finished workout variables
        finExercises = new ArrayList<>();

        setCurrSet();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_curr_workout, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setupEquipView();
        setReps();
        setWeight();
        updateUI();
    }


    // Set up the custom view (EquipmentView) to display the equipment. View added dynamically
    // to trigger onDraw method
    public void setupEquipView() {
        equipmentView = new EquipmentView(act);
        equipmentView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
        ));

        vgEquipDisp.addView(equipmentView, 0);

        equipmentView.post(new Runnable() {
            @Override
            public void run() {
                equipmentView.setup(currSet.getWeight(), currSet.getEquip());
            }
        });
    }

    public void startTimer(long timeInMillis) {
            countDownTimer = getCountDownTimer(timeInMillis);
            countDownTimer.start();
    }

    /* Creates and returns a new CountDownTimer with the rest time to count down from. Has the
     * following format: 0:00
     */
    long currTime;
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
                TextViewCompat.setAutoSizeTextTypeWithDefaults(tvTimer, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            }

            // Changes the timer text, when it gets to 0:00, to "Start the next set"
            public void onFinish() {
                tvTimer.setText(R.string.start_next_set);
                cancel();
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        set_i = 0;
        ex_i = 0;
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////

    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(value = R.id.et_curr_reps, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeRepsChanged() {
        if (!currSet.getIsWarmup())
            lockReps = true;

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

        if (currSet.setReps(reps))
            btnDecReps.setEnabled(false);
    }

    // TextWatcher for weight ET
    @OnTextChanged(value = R.id.et_curr_weight, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeWeightChanged() {
        if (!currSet.getIsWarmup())
            lockWeight = true;

        if (!btnDecWeight.isEnabled())
            btnDecWeight.setEnabled(true);
    }

    @OnTextChanged(value = R.id.et_curr_weight, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onWeightChanged(CharSequence s, int start, int before, int count) {
        float weight;
        if (!s.toString().isEmpty()) {
            weight = Float.valueOf(s.toString());
            equipmentView.setup(weight, currSet.getEquip());
        } else
            weight = currSet.getMinWeight();

        if (currSet.setWeight(weight))
            btnDecWeight.setEnabled(false);
    }
    // =TextWatcher=Handling========================================================================

    // Click Handling
    // =============================================================================================
    // YouTube Vids on how to do an exercise
    @OnClick(R.id.btn_how_to)
    public void startWorkoutsList() {
        Intent intent = new Intent(act, HowToVideos.class);
        intent.putExtra(EXTRA_HOW_TO_VID, currSet.getExName());
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
                currSet.incReps();
                break;
            case R.id.btn_dec_reps:
                currSet.decReps();
                break;
        }
        setReps();
    }

    // Weight change
    @OnClick({R.id.btn_inc_weight, R.id.btn_dec_weight})
    public void changeWeight(ImageButton weightBtn) {
        switch (weightBtn.getId()) {
            case R.id.btn_inc_weight:
                currSet.incWeight();
                break;
            case R.id.btn_dec_weight:
                currSet.decWeight();
                break;
        }
        setWeight();
    }

    @OnClick(R.id.btn_finish_set)
    public void finishSet() {
        if (!currSet.getIsWarmup()  && ex_i == exercises.size() - 1 && set_i == sets.size() - 1) {
            act.finish();
        } else {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            // End of sets
            if (set_i == sets.size()) {
                set_i = 0;


                if (currSet.getIsWarmup()) {
                    currExercise = exercises.get(ex_i);
                } else {
                    lockReps = false;
                    lockWeight = false;
                    ex_i++;
                    currExercise = warmups.get(ex_i);
                }
                currSet.switchSetType();
                sets = currExercise.getSets();
            }

            setCurrSet();
            updateUI();
        }
    }

    public void setCurrSet() {
        currSet.setExName(currExercise.getName());
        currSet.setEquip(currExercise.getEquipment());

        Set set = sets.get(set_i);
        set_i++;

        currSet.setSet(set);
    }

    public void updateUI() {
        String setType;
        if (currSet.getIsWarmup()) {
            setType = "Warmup";
            tvTimer.setText(R.string.start_next_set);

        } else {
            setType = "Main";
            startTimer(5000);
        }

        if (!lockReps)
            setReps();

        if (!lockWeight)
            setWeight();

        tvExInfo.setText(String.format("%s %s/%s",setType, ex_i+1, exercises.size()));
        tvSetInfo.setText(String.format(
                "Set %s/%s",
                currSet.getSetNum(),
                currExercise.getNumSets())
        );
        tvExerciseTitle.setText(currSet.getExName());

    }

    public void setReps() {
        etCurrReps.setText(String.valueOf(currSet.getReps()));
    }

    public void setWeight() {
        float weight = currSet.getWeight();
        etCurrWeight.setText(String.valueOf(weight));
        equipmentView.setup(weight, currSet.getEquip());
    }

    //=Click=Handling===============================================================================
}
