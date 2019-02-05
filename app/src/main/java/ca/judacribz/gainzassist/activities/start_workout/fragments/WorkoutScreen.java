package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.activities.start_workout.CurrWorkout;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.*;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;
import com.orhanobut.logger.Logger;

import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.*;
import static ca.judacribz.gainzassist.models.Exercise.SetsType;
import static ca.judacribz.gainzassist.util.Misc.writeValueAsString;
import static ca.judacribz.gainzassist.util.Preferences.*;
import static ca.judacribz.gainzassist.util.UI.getTextInt;
import static ca.judacribz.gainzassist.util.UI.handleFocusLeft;
import static ca.judacribz.gainzassist.constants.ExerciseConst.*;

public class WorkoutScreen extends Fragment implements
        CurrWorkout.DataListener,
        SingleItemAdapter.ItemClickObserver {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    CurrWorkout currWorkout = CurrWorkout.getInstance();
    StartWorkout act;
    Bundle bundle;
    CountDownTimer countDownTimer;

    SingleItemAdapter
            exerciseAdapter,
            setAdapter;
    LinearLayoutManager
            setManager,
            exerciseManager;

    ArrayList<Exercise> finExercises;
    Exercise updateEx;
    ExerciseSet currSet = null;
    boolean updateSetMode = false;
    long currTime;
    float weight;

    SparseArray<PROGRESS_STATUS>
            exProgress,
            setProgress;

    String setNum;
    // --------------------------------------------------------------------------------------------

    // UI Elements
    @BindView(R.id.equip_view) EquipmentView equipmentView;
    @BindView(R.id.tv_timer) TextView tvTimer;

    @BindView(R.id.tv_exercise_title) TextView tvExerciseTitle;
    @BindView(R.id.tv_set_num) TextView tvSetNum;

    @BindView(R.id.ibtn_dec_reps) ImageButton btnDecReps;
    @BindView(R.id.ibtn_dec_weight) ImageButton btnDecWeight;

    @BindView(R.id.btn_finish_set) Button btnFinishSet;
    @BindView(R.id.btn_update_set) Button btnUpdateSet;
    @BindView(R.id.btn_resume_workout) Button btnResumeWorkout;

    @BindView(R.id.et_reps) EditText etCurrReps;
    @BindView(R.id.et_weight) EditText etCurrWeight;

    @BindView(R.id.rv_exercise_num) RecyclerView rvExercise;
    @BindView(R.id.rv_exercise_set) RecyclerView rvSet;
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

        setNum = "%s " + getString(R.string.set_num);

        currWorkout.setDataListener(this);
        setManager = setProgressLayoutManagers(rvSet);
        exerciseManager = setProgressLayoutManagers(rvExercise);

        rvSet.setHasFixedSize(true);
        rvExercise.setHasFixedSize(true);

        return view;
    }

    private LinearLayoutManager setProgressLayoutManagers(RecyclerView rv) {
        LinearLayoutManager manager = new LinearLayoutManager(
                act,
                LinearLayoutManager.HORIZONTAL,
                false
        );
        rv.setLayoutManager(manager);
        rv.setHasFixedSize(true);

        return manager;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgressExs(currWorkout.getCurrNumExs());
        updateProgSets(currWorkout.getCurrNumSets());

        updateUI();
    }

    public void updateProgressExs(int numExs) {
        exProgress = currWorkout.getExProgress();
        if (exProgress == null) {
            exProgress = setupProgress(numExs, currWorkout.getCurrExNum(), null);
        }
        exerciseAdapter = setupProgressAdapter(
                rvExercise,
                numExs,
                exProgress,
                false
        );

        exerciseAdapter.setItemClickObserver(new ItemClickObserver() {

            @Override
            public void onItemClick(View view) {
                exerciseItemClick(view);
            }

            @Override
            public void onItemLongClick(View view) {
            }
        });
    }

    public void updateProgSets(int numSets) {
        setProgress = currWorkout.getSetProgress();
        if (setProgress == null) {
            setProgress = setupProgress(numSets, currWorkout.getCurrSetNum(), null);
        }
        setAdapter = setupProgressAdapter(
                rvSet,
                numSets,
                setProgress,
                true
        );
    }

    @Override
    public void onPause() {
        super.onPause();

        currWorkout.setDataListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////


    // CurrWorkout.DataListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
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
     *********************************************************************************************/
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

    @Override
    public void updateProgressSets(int numSets) {
        setAdapter = setupProgressAdapter(
                rvSet,
                numSets,
                setProgress = setupProgress(
                        numSets,
                        currWorkout.getCurrSetNum(),
                        currWorkout.getCurrExType()
                ),
                true
        );
    }

    //CurrWorkout.DataListener//Override///////////////////////////////////////////////////////////

    private SingleItemAdapter setupProgressAdapter(RecyclerView rv,
                                                   int numItems,
                                                   SparseArray<PROGRESS_STATUS> progressStatus,
                                                   boolean setClickListener) {
        SingleItemAdapter adapter = new SingleItemAdapter(
                act,
                numItems,
                R.layout.part_text_view_progress,
                R.id.tv_progress,
                progressStatus
        );
        rv.setAdapter(adapter);

        if (setClickListener) {
            adapter.setItemClickObserver(this);
        }

        return adapter;
    }


    private SparseArray<PROGRESS_STATUS> setupProgress(int numItems, int itemInd, SetsType setType) {
        SparseArray<PROGRESS_STATUS> progressStatus = new SparseArray<>();

        for (int i = 0; i < numItems; i++) {
            progressStatus.put(i, UNSELECTED);
        }
        progressStatus.put(itemInd - 1, SELECTED);

        return progressStatus;
    }

    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(value = R.id.et_reps, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeRepsChanged() {
        if (!btnDecReps.isEnabled()) {
            btnDecReps.setEnabled(true);
            btnDecReps.setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(value = R.id.et_reps, callback = OnTextChanged.Callback.TEXT_CHANGED)
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
            value = R.id.et_weight,
            callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeWeightChanged() {
        if (!btnDecWeight.isEnabled()) {
            btnDecWeight.setEnabled(true);
            btnDecWeight.setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(value = R.id.et_weight, callback = OnTextChanged.Callback.TEXT_CHANGED)
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
    @OnFocusChange({R.id.et_reps, R.id.et_weight})
    void onFocusLeft(EditText et, boolean hasFocus) {
        if (!hasFocus) {
            Number min, res;
            switch (et.getId()) {
                case R.id.et_weight:
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
    @OnClick({R.id.ibtn_inc_reps, R.id.ibtn_dec_reps})
    public void changeReps(ImageButton repsBtn) {
        switch (repsBtn.getId()) {
            case R.id.ibtn_inc_reps:
                currWorkout.incReps();
                break;
            case R.id.ibtn_dec_reps:
                currWorkout.decReps();
                break;
        }
        setReps();
    }

    // Weight change
    @OnClick({R.id.ibtn_inc_weight, R.id.ibtn_dec_weight})
    public void changeWeight(ImageButton weightBtn) {
        switch (weightBtn.getId()) {
            case R.id.ibtn_inc_weight:
                currWorkout.incWeight();
                break;
            case R.id.ibtn_dec_weight:
                currWorkout.decWeight();
                break;
        }
        setWeight();
    }

    @OnClick(R.id.btn_resume_workout)
    public void resumeWorkout() {
        updateSetMode = false;

        btnFinishSet.setVisibility(View.VISIBLE);
        btnUpdateSet.setVisibility(View.INVISIBLE);
        btnResumeWorkout.setVisibility(View.INVISIBLE);

        setAdapter = setupProgressAdapter(
                rvSet,
                currWorkout.getCurrNumSets(),
                setProgress,
                true
        );

        exerciseAdapter.setSelected(currWorkout.getCurrExNum());

        tvExerciseTitle.setText(currWorkout.getCurrExName());
        equipmentView.setup(currSet.getWeight(), currWorkout.getCurrEquip());
        etCurrReps.setText(String.valueOf(currSet.getReps()));
        etCurrWeight.setText(String.valueOf(currSet.getWeight()));

        updateUI();

        currSet = null;
    }

    // Finish set
    @OnClick(R.id.btn_finish_set)
    public void finishSet() {
        if (currWorkout.finishCurrSet()) {
            updateUI();

        // End of workout
        } else {
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

        if (!currWorkout.getLockReps()) {
            setReps();
        }

        if (!currWorkout.getLockWeight()) {
            setWeight();
        }

        tvExerciseTitle.setText(currWorkout.getCurrExName());

        selectProgressAdapterPos(
                exerciseAdapter,
                rvExercise,
                currWorkout.getCurrExNum(),
                currWorkout.getExSuccess());

        tvSetNum.setText(String.format(setNum, setType));
        selectProgressAdapterPos(
                setAdapter,
                rvSet,
                currWorkout.getCurrSetNum(),
                currWorkout.getSetSuccess()
        );
    }

    ArrayList<ExerciseSet> setsToUpdate;

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

    private void selectProgressAdapterPos(
            SingleItemAdapter adapter,
            RecyclerView rv,
            int pos, boolean success) {

        adapter.setCurrItem(pos, success);
        rv.scrollToPosition(pos - 1);
    }

    // SingleItemAdapter.ItemClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void exerciseItemClick(View view) {
        int ind = getTextInt((TextView) view);
        updateEx = currWorkout.getSessionExercise(ind);

        if (updateEx != null) {
            SparseArray<PROGRESS_STATUS> setStatus = new SparseArray<>();

            updateSetMode = true;

            if (btnFinishSet.getVisibility() == View.VISIBLE) {
                btnFinishSet.setVisibility(View.INVISIBLE);
                btnUpdateSet.setVisibility(View.VISIBLE);
                btnResumeWorkout.setVisibility(View.VISIBLE);
            }
            if (currSet == null) {
                currSet = new ExerciseSet(
                        updateEx,
                        currWorkout.getCurrSetNum(),
                        currWorkout.getCurrReps(),
                        currWorkout.getCurrWeight()
                );
            }

            exerciseAdapter.setSelected(ind);
            setsToUpdate = updateEx.getFinishedSetsList();
            for (ExerciseSet set : setsToUpdate) {
                if (set.getReps() >= updateEx.getReps() && set.getWeight() >= updateEx.getWeight()) {
                    setStatus.put(set.getSetNumber(), SUCCESS);
                } else {
                    setStatus.put(set.getSetNumber(), FAIL);
                }
            }

            setAdapter = setupProgressAdapter(
                    rvSet,
                    updateEx.getNumSets(),
                    setStatus,
                    true
            );

            setAdapter.setSelected(1);

            updateUI(updateEx, 0);
        }
    }

    @Override
    public void onItemClick(View view) {
        int ind = getTextInt((TextView) view);

        if (updateSetMode || (!currWorkout.getIsWarmup() && ind < currWorkout.getCurrSetNum())) {
            setAdapter.setSelected(ind);
            updateUI(updateEx, ind - 1);
        }
    }

    private void updateUI(Exercise updateEx, int setInd) {
        ExerciseSet set = updateEx.getFinishedSetsList().get(setInd);

        tvExerciseTitle.setText(updateEx.getName());
        tvSetNum.setText(String.format(setNum, "Main"));

        equipmentView.setup(set.getWeight(), updateEx.getEquipment());
        tvTimer.setText(R.string.update_set);
        etCurrReps.setText(String.valueOf(set.getReps()));
        etCurrWeight.setText(String.valueOf(set.getWeight()));
    }

    @Override
    public void onItemLongClick(View view) {

    }
    //SingleItemAdapter.ItemClickObserver//Override////////////////////////////////////////////////
    //=Click=Handling===============================================================================

}
