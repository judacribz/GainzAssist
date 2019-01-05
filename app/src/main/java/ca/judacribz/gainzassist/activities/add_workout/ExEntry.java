package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.models.Exercise;

import static ca.judacribz.gainzassist.constants.ExerciseConst.*;
import static ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET;
import static ca.judacribz.gainzassist.util.UI.*;

public class ExEntry extends Fragment {
    ExEntryDataListener exEntryDataListener;
    public interface ExEntryDataListener {
        boolean checkExerciseExists(ExEntry fmt, String exerciseName);
        void exerciseDataReceived(Exercise exercise);
        void deleteExercise(@Nullable Exercise exercise, int index);
        void cancelWorkout();
    }

    Exercise exercise = null;
    String exerciseName;

    int num_reps, num_sets, ex_i = 0, minInt = 1; // for min num_reps/num_sets
    float weight, minWeight, weightChange;

    @BindView(R.id.et_exercise_name) EditText etExerciseName;
    @BindView(R.id.et_weight) EditText etWeight;
    @BindView(R.id.et_num_reps) EditText etNumReps;
    @BindView(R.id.et_num_sets) EditText etNumSets;

    @BindView(R.id.ibtn_dec_weight) ImageButton ibtnDecWeight;
    @BindView(R.id.ibtn_dec_reps) ImageButton ibtnDecReps;
    @BindView(R.id.ibtn_dec_sets) ImageButton ibtnDecSets;

    @BindView(R.id.spr_equipment) Spinner sprEquipment;

    @BindView(R.id.btn_update) Button btnUpdate;

    @BindViews({R.id.et_exercise_name, R.id.et_weight, R.id.et_num_reps, R.id.et_num_sets})
    EditText[] formEntries;

    public ExEntry() {
        // Required empty public constructor
    }

    public void setInd(int index) {
        this.ex_i = index;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ExEntryDataListener) {
            exEntryDataListener = (ExEntryDataListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ExEntryDataListener");
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_ex_entry, container, false);
        ButterKnife.bind(this, view);

        num_reps = Integer.valueOf(getString(R.string.starting_reps));
        num_sets = Integer.valueOf(getString(R.string.starting_sets));

        setSpinnerWithArray(getActivity(), R.array.exerciseEquipment, sprEquipment);

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        exEntryDataListener = null;
    }


    @OnItemSelected(R.id.spr_equipment)
    public void equipmentSelected(int position) {
        switch (position) {
            case 0:
                minWeight = BB_MIN_WEIGHT;
                weightChange = BB_WEIGHT_CHANGE;
                break;
            case 1:
                minWeight = DB_MIN_WEIGHT;
                weightChange = DB_WEIGHT_CHANGE;
            default:
                minWeight = MIN_WEIGHT;
                weightChange = WEIGHT_CHANGE;
                break;
        }


        if (weight > minWeight) {

            if (!ibtnDecWeight.isEnabled()) {
                ibtnDecWeight.setEnabled(true);
                ibtnDecWeight.setVisibility(View.VISIBLE);
            }
        } else {
            etWeight.setText(String.valueOf(minWeight));

            if (ibtnDecWeight.isEnabled()) {
                ibtnDecWeight.setEnabled(false);
                ibtnDecWeight.setVisibility(View.GONE);
            }
        }

    }

    // OnTextChanged Handling
    // =============================================================================================
    @OnTextChanged(value = R.id.et_weight, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeNumExercisesChanged() {
        if (!ibtnDecWeight.isEnabled()) {
            ibtnDecWeight.setEnabled(true);
            ibtnDecWeight.setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(value = R.id.et_weight, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onNumExercisesChanged(CharSequence s, int start, int before, int count) {
        String weightStr = s.toString();
        weight = (weightStr.isEmpty()) ? minWeight : Float.valueOf(weightStr);

        if (weight <= minWeight) {
            ibtnDecWeight.setEnabled(false);
            ibtnDecWeight.setVisibility(View.GONE);
        }

    }

    @OnTextChanged(value = R.id.et_num_reps, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeRepsChanged() {
        beforeNumChanged(ibtnDecReps);
    }

    @OnTextChanged(value = R.id.et_num_sets, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeSetsChanged() {
        beforeNumChanged(ibtnDecSets);
    }

    public void beforeNumChanged(ImageButton ibtnDec) {
        if (!ibtnDec.isEnabled()) {
            ibtnDec.setEnabled(true);
            ibtnDec.setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(R.id.et_num_reps)
    public void onRepsChanged(CharSequence s,
                              int start,
                              int before,
                              int count) {
        num_reps = onNumChanged(ibtnDecReps, s.toString());
    }

    @OnTextChanged(R.id.et_num_sets)
    public void onSetsChanged(CharSequence s,
                              int start,
                              int before,
                              int count) {
        num_sets = onNumChanged(ibtnDecSets, s.toString());
    }

    public int onNumChanged(ImageButton ibtnDec, String str) {
        int value = (str.isEmpty()) ? minInt : Integer.valueOf(str);

        if (value <= minInt) {
            ibtnDec.setEnabled(false);
            ibtnDec.setVisibility(View.GONE);
        }

        return value;
    }
    // =OnTextChanged=Handling======================================================================

    // OnFocusChanged Handling
    // =============================================================================================
    @OnFocusChange({R.id.et_num_reps, R.id.et_num_sets, R.id.et_weight})
    void onFocusLeft(EditText et, boolean hasFocus) {
        if (!hasFocus) {
            Number min = 0, res = 0;
            switch (et.getId()) {
                case R.id.et_weight:
                    res = weight;
                    min = minWeight;
                    break;
                case R.id.et_num_reps:
                    res = num_reps;
                    min = minInt;
                    break;
                case R.id.et_num_sets:
                    res = num_sets;
                    min = minInt;
                    break;
            }

            handleFocusLeft(et, min, res);
        }

    }
    // =OnFocusChanged=Handling=====================================================================

    // Click Handling
    // =============================================================================================
    /* Increase weight */
    @OnClick(R.id.ibtn_inc_weight)
    public void incNumWeight() {
        etWeight.setText(String.valueOf(weight + weightChange));
    }

    /* Decrease weight */
    @OnClick(R.id.ibtn_dec_weight)
    public void decNumWeight() {
        etWeight.setText(String.valueOf(Math.max(weight - weightChange, minWeight)));
    }

    /* Increase num_reps */
    @OnClick(R.id.ibtn_inc_reps)
    public void incNumReps() {
        etNumReps.setText(String.valueOf(num_reps + 1));
    }

    /* Decrease num_reps */
    @OnClick(R.id.ibtn_dec_reps)
    public void decNumReps() {
        etNumReps.setText(String.valueOf(num_reps - 1));
    }

    /* Increase num_sets */
    @OnClick(R.id.ibtn_inc_sets)
    public void incNumSets() {
        etNumSets.setText(String.valueOf(num_sets + 1));
    }

    /* Decrease num_sets */
    @OnClick(R.id.ibtn_dec_sets)
    public void decNumSets() {
        etNumSets.setText(String.valueOf(num_sets - 1));
    }

    @OnClick(R.id.btn_enter)
    public void enterExercise(Button btnEnter) {

        if (validateForm(getActivity(), formEntries)) {
            exerciseName = getTextString(etExerciseName);

            // Check if exercise already added
            if (!exEntryDataListener.checkExerciseExists(this, exerciseName)) {
                btnEnter.setVisibility(View.GONE);
                btnUpdate.setVisibility(View.VISIBLE);

                num_reps = getTextInt(etNumReps);
                num_sets = getTextInt(etNumSets);

                exEntryDataListener.exerciseDataReceived(exercise = new Exercise(
                        this.ex_i,
                        exerciseName,
                        "Strength",
                        getTextString(sprEquipment),
                        getTextInt(etNumSets),
                        getTextInt(etNumReps),
                        getTextFloat(etWeight),
                        MAIN_SET
                ));
            }
        }
    }

    public void setExerciseExists() {
        etExerciseName.setError(String.format(
                getString(R.string.err_exercise_exists),
                exerciseName
        ));
    }

    @OnClick(R.id.btn_update)
    public void updateExercise() {
    }

    @OnClick(R.id.btn_delete)
    public void deleteExercise() {
        etExerciseName.setText("");
        exEntryDataListener.deleteExercise(exercise, this.ex_i);
    }

    @OnClick(R.id.btn_cancel)
    public void cancelWorkout() {
        exEntryDataListener.cancelWorkout();
    }
    //=Click=Handling===============================================================================
}
