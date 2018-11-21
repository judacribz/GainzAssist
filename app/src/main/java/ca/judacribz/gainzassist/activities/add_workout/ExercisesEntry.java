package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Workout;
import org.parceler.Parcels;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.CALLING_ACTIVITY.*;
import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.*;
import static ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.*;
import static ca.judacribz.gainzassist.activities.start_workout.CurrWorkout.*;
import static ca.judacribz.gainzassist.util.UI.*;

public class ExercisesEntry extends AppCompatActivity {

    // Constants
    // --------------------------------------------------------------------------------------------

    public static final int REQ_NEW_WORKOUT_SUMMARY = 1002;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    Workout workout;
    ArrayList<ExerciseSet> exerciseSets;
    String workoutName, exerciseName;

    int numExs, num_reps, num_sets, ex_i = 0, minInt = 1; // for min num_reps/num_sets
    float weight, minWeight, weightChange;

    @BindView(R.id.et_exercise_name) EditText etExerciseName;
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
                String.format(getString(R.string.title_exercises_entry), ex_i),
                true
        );
        workout = new Workout();

        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipment);

        Intent workoutEntryIntent = getIntent();
        workout.setName(workoutEntryIntent.getStringExtra(EXTRA_WORKOUT_NAME));
        numExs = workoutEntryIntent.getIntExtra(EXTRA_NUM_EXERCISES, MIN_NUM);
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


    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == REQ_NEW_WORKOUT_SUMMARY) {
            finish();
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
        num_reps = onNumChanged(etNumReps, ibtnDecReps, s.toString());
    }

    @OnTextChanged(R.id.et_num_sets)
    public void onSetsChanged(CharSequence s,
                              int start,
                              int before,
                              int count) {
        num_sets = onNumChanged(etNumSets, ibtnDecSets, s.toString());
    }

    public int onNumChanged(EditText etNum, ImageButton ibtnDec, String str) {
        int value = (str.isEmpty()) ? minInt : Integer.valueOf(str);

        if (value <= minInt) {
            ibtnDec.setEnabled(false);
            ibtnDec.setVisibility(View.GONE);

            if (value < minInt)
                etNum.setText(String.valueOf(minInt));
        }

        return value;
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
    /* Increase num_reps */
    @OnClick(R.id.ibtn_inc_reps)
    public void incNumReps() {
        etNumReps.setText(String.valueOf(getTextInt(etNumReps) + 1));
    }

    /* Decrease num_reps */
    @OnClick(R.id.ibtn_dec_reps)
    public void decNumReps() {
        etNumReps.setText(String.valueOf(getTextInt(etNumReps) - 1));
    }

    /* Increase num_sets */
    @OnClick(R.id.ibtn_inc_sets)
    public void incNumSets() {
        etNumSets.setText(String.valueOf(getTextInt(etNumSets) + 1));
    }

    /* Decrease num_sets */
    @OnClick(R.id.ibtn_dec_sets)
    public void decNumSets() {
        etNumSets.setText(String.valueOf(getTextInt(etNumSets) - 1));
    }

    @OnClick(R.id.btn_enter)
    public void enterExercise() {

        if (validateFormEntry(this, etExerciseName)) {
            exerciseName = getTextString(etExerciseName);

            // Check if exercise already added
            if (workout.containsExercise(exerciseName)) {
                etExerciseName.setError(String.format(
                        getString(R.string.err_exercise_exists),
                        exerciseName
                ));
            } else {
//                exerciseSets = new ArrayList<>();
                num_reps = getTextInt(etNumReps);
                num_sets = getTextInt(etNumSets);
//                for (int i = 1; i <= num_sets; i++) {
//                    exerciseSets.add(new ExerciseSet(i, num_reps, weight));
//                }
                workout.addExercise(new Exercise(
                        ex_i,
                        exerciseName,
                        "Strength",
                        getTextString(sprEquipment),
                        getTextInt(etNumSets),
                        getTextInt(etNumReps),
                        getTextFloat(etWeight)
                ));


                ex_i++;
                if (ex_i >= numExs) {
                    Intent newWorkoutSummaryIntent = new Intent(this, NewWorkoutSummary.class);
                    newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout));
                    newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, EXERCISES_ENTRY);
                    startActivityForResult(newWorkoutSummaryIntent, REQ_NEW_WORKOUT_SUMMARY);
                }

                etExerciseName.setText("");
            }
        }
    }

    @OnClick(R.id.btn_cancel)
    public void cancelWorkout() {
        finish();
    }
    //=Click=Handling===============================================================================

}
