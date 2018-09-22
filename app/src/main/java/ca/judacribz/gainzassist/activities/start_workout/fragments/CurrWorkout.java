package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos;
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.CurrSet;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.CurrUser;

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

    CurrSet currSet;

    Exercise currExercise;
    ArrayList<Set> sets;
    ArrayList<Exercise> warmups, exercises;
    int set_i = 0, ex_i = 0;

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
        currSet = CurrSet.getInstance();

        warmups = CurrUser.getInstance().getWarmups();
        currExercise = warmups.get(ex_i);
        exercises = StartWorkout.workout.getExercises();
        sets = currExercise.getSets();

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

        tvExerciseTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f);
        tvSetInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        tvExInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupEquipView();
        setRepsWeight();
        updateUI();
        startTimer(5000);
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
                int reps;
                String repStr = s.toString();
                if (!repStr.isEmpty()) {
                    reps = Integer.valueOf(repStr);
                } else {
                    reps = MIN_REPS;
                }

                if (currSet.setReps(reps)) {
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
                if (!btnDecWeight.isEnabled())
                    btnDecWeight.setEnabled(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float weight;
                if (!s.toString().isEmpty()) {
                    weight = Float.valueOf(s.toString());
                    equipmentView.setup(weight, currSet.getEquip());
                } else {
                    weight = currSet.getMinWeight();
                }


                if (currSet.setWeight(weight)) {
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
                this.cancel();
                tvTimer.setText(R.string.start_next_set);
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
        intent.putExtra(EXTRA_HOW_TO_VID, currSet.getExName());
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
        currSet.decReps();
        setReps();
    }

    @OnClick(R.id.btn_inc_reps)
    public void incReps() {
        currSet.incReps();
        setReps();
    }

    public void setReps() {
        etCurrReps.setText(String.valueOf(currSet.getReps()));
    }

    @OnClick(R.id.btn_dec_weight)
    public void decWeight() {
        currSet.decWeight();
        setWeight();
    }

    @OnClick(R.id.btn_inc_weight)
    public void incWeight() {
        currSet.incWeight();
        setWeight();
    }

    public void setWeight() {
        float weight = currSet.getWeight();
        etCurrWeight.setText(String.valueOf(weight));
        equipmentView.setup(weight, currSet.getEquip());
    }

    @OnClick(R.id.btn_finish_set)
    public void finishSet() {
        if (!currSet.getIsWarmup()  && ex_i == exercises.size() - 1 && set_i == sets.size() - 1) {
                act.finish();
        } else {

            countDownTimer.cancel();
            startTimer(5000);

            // End of sets
            if (set_i == sets.size()) {
                set_i = 0;


                if (currSet.getIsWarmup()) {
                    currExercise = exercises.get(ex_i);
                } else {
                    ++ex_i;
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
        currSet.setSet(sets.get(set_i++));
    }

    public void updateUI() {
//        String info = "Warmup 1/" + warmups.size() + "\nSet 1/" + currExercise.getNumSets();
        String setType;
        if (currSet.getIsWarmup()) {
            setType = "Warmup";
        } else {
            setType = "Main";
        }

        tvExInfo.setText(String.format("%s %s/%s",setType, ex_i+1, warmups.size()));
        tvSetInfo.setText(String.format("Set %s/%s", currSet.getSetNum(), currExercise.getNumSets()));
        tvExerciseTitle.setText(currSet.getExName());
    }

    //=Click=Handling==============================================================================
}
