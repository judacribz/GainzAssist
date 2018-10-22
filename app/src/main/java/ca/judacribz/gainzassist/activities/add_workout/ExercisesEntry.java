package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import butterknife.*;
import ca.judacribz.gainzassist.R;

import static ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.*;
import static ca.judacribz.gainzassist.models.CurrWorkout.*;
import static ca.judacribz.gainzassist.util.UI.getTextFloat;
import static ca.judacribz.gainzassist.util.UI.setInitView;
import static ca.judacribz.gainzassist.util.UI.setSpinnerWithArray;

public class ExercisesEntry extends AppCompatActivity {

    // Constants
    // --------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    String workoutName;
    int numExs, ex_i = MIN_NUM_EXERCISES;
    float minWeight, weightChange;

    @BindView(R.id.et_weight) EditText etWeight;
    @BindView(R.id.et_num_reps) EditText etNumReps;
    @BindView(R.id.et_num_sets) EditText etNumSets;

    @BindView(R.id.ibtn_dec_weight) ImageButton ibtnDecWeight;
    @BindView(R.id.ibtn_dec_reps) ImageButton ibtnDecReps;
    @BindView(R.id.ibtn_dec_sets) ImageButton ibtnDecSets;

    @BindView(R.id.spr_equipment) Spinner sprEquipment;
    // --------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(
                this,
                R.layout.activity_exercises_entry,
                String.format(getString(R.string.exercise_num), ex_i),
                true
        );

        ibtnDecWeight.setEnabled(false);
        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipment);

        Intent workoutEntryIntent = getIntent();
        workoutName = workoutEntryIntent.getStringExtra(EXTRA_WORKOUT_NAME);
        numExs = workoutEntryIntent.getIntExtra(EXTRA_NUM_EXERCISES, MIN_NUM_EXERCISES);
    }

    @OnItemSelected(R.id.spr_equipment)
    public void equipmentSelected(Spinner spinner, int position) {
        switch (position) {
            case 0:
                minWeight = BB_MIN_WEIGHT;
                weightChange = BB_WEIGHT_CHANGE;
                break;
            default:
                minWeight = MIN_WEIGHT;
                weightChange = WEIGHT_CHANGE;
                break;
        }


        if (!ibtnDecWeight.isEnabled() || weight < minWeight){
            etWeight.setText(String.valueOf(minWeight));
        }

    }


    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(value = R.id.et_weight, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeNumExercisesChanged() {
        if (!ibtnDecWeight.isEnabled()) {
            ibtnDecWeight.setEnabled(true);
            ibtnDecWeight.setVisibility(View.VISIBLE);
        }
    }


    float weight;
    @OnTextChanged(value = R.id.et_weight, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onNumExercisesChanged(CharSequence s, int start, int before, int count) {
        String weightStr = s.toString();
        weight = (weightStr.isEmpty()) ? minWeight : Float.valueOf(weightStr);

        if (weight <= minWeight) {
            ibtnDecWeight.setEnabled(false);
            ibtnDecWeight.setVisibility(View.GONE);

            if (weight < minWeight)
                etWeight.setText(String.valueOf(minWeight));
        }

    }
    // =TextWatcher=Handling========================================================================

    // Click Handling
    // =============================================================================================
    /* Increase weight */
    @OnClick(R.id.ibtn_inc_weight)
    public void incNumWeight() {
        etWeight.setText(String.valueOf(getTextFloat(etWeight) + weightChange));

    }
    /* Decrease weight */
    @OnClick(R.id.ibtn_dec_weight)
    public void decNumWeight() {
        etWeight.setText(String.valueOf(Math.max(getTextFloat(etWeight) - weightChange, minWeight)));
    }
    /* Increase reps */
    @OnClick(R.id.ibtn_inc_reps)
    public void incNumReps() {
    }

    /* Decrease reps */
    @OnClick(R.id.ibtn_dec_reps)
    public void decNumReps() {
    }
    /* Increase sets */
    @OnClick(R.id.ibtn_inc_sets)
    public void incNumSets() {
    }

    /* Decrease sets */
    @OnClick(R.id.ibtn_dec_sets)
    public void decNumSets() {
    }

    @OnClick(R.id.btn_enter)
    public void enterWorkoutName() {

    }

    @OnClick(R.id.btn_cancel)
    public void cancelWorkout() {
        finish();
    }
    //=Click=Handling===============================================================================

}
