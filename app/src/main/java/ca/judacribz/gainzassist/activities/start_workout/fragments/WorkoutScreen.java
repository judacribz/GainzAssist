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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos;
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.CurrWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.CurrUser;
import ca.judacribz.gainzassist.models.Workout;
//
//import static ca.judacribz.gainzassist.activities.start_workout.WorkoutPagerAdapter.EXTRA_WARMUPS;
//import static ca.judacribz.gainzassist.activities.start_workout.WorkoutPagerAdapter.EXTRA_WORKOUT;
import static ca.judacribz.gainzassist.models.CurrWorkout.MIN_REPS;

public class WorkoutScreen extends Fragment {

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
    CurrUser currUser = CurrUser.getInstance();

    Exercise currExercise;
    ArrayList<Set> sets;
    ArrayList<Exercise> warmups = new ArrayList<>(),
                        exercises = new ArrayList<>();
    int set_i = 0, ex_i = 0;

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
String TAG = "WorkoutScreen";
    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (StartWorkout) context;

        // init finished workout variables
        finExercises = new ArrayList<>();

//        setCurrSet();
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
                equipmentView.setup(currWorkout.getWeight(), currWorkout.getEquip());
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

    @Override
    public void onStop() {
        super.onStop();
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

        if (currWorkout.setReps(reps))
            btnDecReps.setEnabled(false);
    }

    // TextWatcher for weight ET
    @OnTextChanged(value = R.id.et_curr_weight, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeWeightChanged() {
        if (!btnDecWeight.isEnabled())
            btnDecWeight.setEnabled(true);
    }

    @OnTextChanged(value = R.id.et_curr_weight, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onWeightChanged(CharSequence s, int start, int before, int count) {
        float weight;
        if (!s.toString().isEmpty()) {
            weight = Float.valueOf(s.toString());
            equipmentView.setup(weight, currWorkout.getEquip());
        } else
            weight = currWorkout.getMinWeight();

        if (currWorkout.setWeight(weight))
            btnDecWeight.setEnabled(false);
    }
    // =TextWatcher=Handling========================================================================

    // Click Handling
    // =============================================================================================
    @OnClick(R.id.btn_how_to)
    public void startWorkoutsList() {
        Intent intent = new Intent(act, HowToVideos.class);
        intent.putExtra(EXTRA_HOW_TO_VID, currWorkout.getExName());
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
//        setReps();
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
        if (!currWorkout.getIsWarmup()  && ex_i == exercises.size() - 1 && set_i == sets.size() - 1) {
            act.finish();
        } else {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

//            setCurrSet();
            updateUI();
        }
    }

    public void updateUI() {
        String setType;
        if (currWorkout.getIsWarmup()) {
            setType = "Warmup";
            tvTimer.setText(R.string.start_next_set);
        } else {
            setType = "Main";
            startTimer(5000);
        }

//        setReps();
        setWeight();

        tvExInfo.setText(String.format("%s %s/%s",setType, ex_i+1, exercises.size()));
        tvSetInfo.setText(String.format(
                "Set %s/%s",
                currWorkout.getSetNum(),
                currExercise.getNumSets())
        );
        tvExerciseTitle.setText(currWorkout.getExName());

    }

    public void setWeight() {
        float weight = currWorkout.getWeight();
        etCurrWeight.setText(String.valueOf(weight));
        equipmentView.setup(weight, currWorkout.getEquip());
    }

    //=Click=Handling===============================================================================
}
