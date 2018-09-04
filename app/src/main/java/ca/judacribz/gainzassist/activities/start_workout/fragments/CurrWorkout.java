package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos;
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.User;

public class CurrWorkout extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_HOW_TO_VID = "ca.judacribz.gainzassist.EXTRA_HOW_TO_VID";
    public static final float BB_MIN_WEIGHT = 45.0f;
    public static final float MIN_WEIGHT = 0.0f;
    public static final float BB_WEIGHT_CHANGE = 5.0f;
    public static final float WEIGHT_CHANGE = 2.5f;
    public static final int MIN_REPS = 0;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    EquipmentView equipmentView;
    CountDownTimer countDownTimer;
    Exercise currExercise;
    ArrayList<Set> currSets;
    Set currSet;
    String currExerciseName, currEquipment;
    ArrayList<Exercise> warmups;

    float currWeight, weight, minWeight = MIN_WEIGHT, weightChange = WEIGHT_CHANGE;
    int currReps, reps;



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
        act = (StartWorkout) getActivity();

        warmups = User.getInstance().getWarmups();

        currExercise = warmups.get(0);
        currExerciseName = currExercise.getName();
        currSets = currExercise.getSets();
        currSet = currSets.get(0);
        weight = currWeight = currSet.getWeight();
        reps = currReps = currSet.getReps();

        currEquipment = currExercise.getEquipment();

        if (currEquipment.toLowerCase().equals(getString(R.string.barbell))) {
            minWeight = BB_MIN_WEIGHT;
            weightChange = BB_WEIGHT_CHANGE;
        }
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
        View view = inflater.inflate(R.layout.fragment_curr_workout, container, false);
        ButterKnife.bind(this, view);

        String info = "Warmup 1/" + warmups.size() + "\nSet 1/" + currExercise.getNumSets();
        tvExInfo.setText(String.format("Warmup 1/%s", warmups.size()));
        tvSetInfo.setText(String.format("Set 1/%s", currExercise.getNumSets()));
        tvExerciseTitle.setText(currExerciseName);

        setupEquipView();
        setRepsWeight();
        startTimer(500000);

        return view;
    }

    private void setRepsWeight() {
        setReps();
        setWeight();

        etCurrReps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!btnDecReps.isEnabled())
                    btnDecReps.setEnabled(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    reps = Integer.valueOf(s.toString());
                } else {
                    reps = MIN_REPS;
                }

                if (reps == MIN_REPS) {
                    btnDecReps.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etCurrWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!btnDecWeight.isEnabled()) {
                    btnDecWeight.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    weight = Float.valueOf(s.toString());
                    equipmentView.setup(weight, getString(R.string.barbell));
                } else {
                    weight = minWeight;
                }

                if ((weight - weightChange) < minWeight) {
                    btnDecWeight.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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

        setEquipViewWeight();
    }

    public void setEquipViewWeight() {
        equipmentView.post(new Runnable() {
            @Override
            public void run() {
                equipmentView.setup(weight, getString(R.string.barbell));
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
                tvTimer.setText(timerText);
            }

            // Changes the timer text, when it gets to 0:00, to "Start the next set"
            public void onFinish() {
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        countDownTimer.cancel();
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================
    @OnClick(R.id.btn_how_to)
    public void startWorkoutsList() {
        Intent intent = new Intent(act, HowToVideos.class);
        intent.putExtra(EXTRA_HOW_TO_VID, currExerciseName);
        startActivity(intent);
    }

    @OnClick(R.id.tv_timer)
    public void changeTimerState() {
        if (currTime/1000 != 0) {
            if (countDownTimer == null) {
                startTimer(currTime);
            } else {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }
    }

    @OnClick(R.id.btn_dec_reps)
    public void decReps() {
        reps--;
        setReps();
    }

    @OnClick(R.id.btn_inc_reps)
    public void incReps() {
        reps++;
        setReps();
    }

    public void setReps() {
        etCurrReps.setText(String.valueOf(reps));
    }

    @OnClick(R.id.btn_dec_weight)
    public void decWeight() {
        weight -= weightChange;
        setWeight();
    }

    @OnClick(R.id.btn_inc_weight)
    public void incWeight() {
        weight += weightChange;
        setWeight();
    }

    public void setWeight() {
        etCurrWeight.setText(String.valueOf(Math.max(weight, minWeight)));
        equipmentView.setup(weight, getString(R.string.barbell));
    }
    //=Click=Handling==============================================================================
}
