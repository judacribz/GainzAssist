package ca.judacribz.gainzassist.activities.add_workout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.WorkoutHelper;

import static ca.judacribz.gainzassist.activities.add_workout.ExercisesEntry.EXTRA_WORKOUT;
import static ca.judacribz.gainzassist.firebase.Database.addWorkoutFirebase;
import static ca.judacribz.gainzassist.models.CurrWorkout.*;
import static ca.judacribz.gainzassist.models.Exercise.*;
import static ca.judacribz.gainzassist.util.Calculations.getNumColumns;
import static ca.judacribz.gainzassist.util.UI.*;

public class NewWorkoutSummary extends AppCompatActivity implements SingleItemAdapter.ItemClickObserver {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final int MIN_INT = 1;
    private static final float MIN_FLOAT = 5.0f;

    private static final int POS_STREN = 0;
    private static final int POS_CARDIO = 1;

    private static final int POS_NA = 2;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    SingleItemAdapter exerciseAdapter;

    ArrayList<Exercise> exercises;
    ArrayList<Set> exSets;
    Workout workout;

    int num_reps, num_sets, minInt = 1; // for min num_reps/num_sets
    float weight, minWeight, weightChange;

    WorkoutHelper workoutHelper;

    // UI Elements
    @BindView(R.id.et_workout_name) EditText etWorkoutName;

    @BindView(R.id.et_exercise_name) EditText etExerciseName;
    @BindView(R.id.spr_type) Spinner sprType;
    @BindView(R.id.spr_equipment) Spinner sprEquipment;
    @BindView(R.id.et_num_reps) EditText etNumReps;
    @BindView(R.id.et_weight) EditText etWeight;
    @BindView(R.id.et_num_sets) EditText etNumSets;

    @BindView(R.id.btn_dec_reps) ImageButton ibtnDecReps;
    @BindView(R.id.btn_dec_weight) ImageButton ibtnDecWeight;
    @BindView(R.id.btn_dec_sets) ImageButton ibtnDecSets;

    @BindView(R.id.btn_add_exercise) Button btnAddExercise;
    @BindView(R.id.btn_update_exercise) Button btnUpdateExercise;

    @BindView(R.id.rv_exercise_btns) RecyclerView rvExerciseList;


    EditText[] formEntries;
    // --------------------------------------------------------------------------------------------


    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setInitView(this, R.layout.activity_new_workout_summary, R.string.title_new_workout_summary, true);

        workout = getIntent().getParcelableExtra(EXTRA_WORKOUT);
        formEntries = new EditText[]{etExerciseName, etNumReps, etWeight, etNumSets};


        String workoutName = workout.getName();
        if (workoutName != null) {
            etWorkoutName.setText(workoutName);
        }

        setSpinnerWithArray(this, R.array.exerciseType, sprType);
        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipment);

        // Set the layout manager
        rvExerciseList.setLayoutManager(new GridLayoutManager(
                this,
                getNumColumns(this),
                GridLayoutManager.HORIZONTAL,
                false
        ));
        rvExerciseList.setHasFixedSize(true);


        exercises = workout.getExercises();
        updateAdapter();

        etWorkoutName.setText(workout.getName());

        workoutHelper = new WorkoutHelper(this);
    }


    /* Toolbar back arrow handling */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////

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
    @OnTextChanged(value = R.id.et_exercise_name, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onExerciseNameChanged(CharSequence s, int start, int before, int count) {
        String exerciseName = s.toString();

        if (!exerciseName.isEmpty()) {
            if (workout.containsExercise(exerciseName)) {
                switchExerciseBtns(btnAddExercise, btnUpdateExercise);
            } else {
                switchExerciseBtns(btnUpdateExercise, btnAddExercise);
            }
        } else {
            switchExerciseBtns(btnUpdateExercise, btnAddExercise);
        }
    }

    private void switchExerciseBtns(Button btnDisable, Button btnEnable) {
        if (btnDisable.getVisibility() == View.VISIBLE) {
            btnEnable.setVisibility(View.VISIBLE);
            btnDisable.setVisibility(View.GONE);
        }
    }


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


    // SingleItemAdapter.ItemClickObserver override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onWorkoutClick(String exName) {
        updateExerciseArea(exName);
    }
Exercise ex;
    private void updateExerciseArea(String exName) {
        ex = workout.getExercise(exName);
        etExerciseName.setText(exName);
        etNumSets.setText(String.valueOf(ex.getNumSets()));
        etNumReps.setText(String.valueOf(ex.getNumSets()));
        etWeight.setText(String.valueOf(ex.getAvgWeight()));
        sprEquipment.setSelection(EQUIPMENT_TYPES.indexOf(ex.getEquipment()));
        sprType.setSelection(EXERCISE_TYPES.indexOf(ex.getType()));
    }


    @Override
    public void onWorkoutLongClick(View anchor, String name) {

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Click Handling
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Increase number of num_reps */
    @OnClick(R.id.btn_inc_reps)
    public void incReps() {
        etNumReps.setText(String.valueOf(getTextInt(etNumReps) + MIN_INT));
    }

    /* Decrease number of num_reps */
    @OnClick(R.id.btn_dec_reps)
    public void decReps() {
        etNumReps.setText(String.valueOf(Math.max(getTextInt(etNumReps) - MIN_INT, MIN_INT)));
    }

    /* Increase number of num_sets */
    @OnClick(R.id.btn_inc_sets)
    public void incSets() {
        etNumSets.setText(String.valueOf(getTextInt(etNumSets) + MIN_INT));
    }

    /* Decrease number of num_sets */
    @OnClick(R.id.btn_dec_sets)
    public void decSets() {
        etNumSets.setText(String.valueOf(Math.max(getTextInt(etNumSets) - MIN_INT, MIN_INT)));
    }

    /* Increase weight */
    @OnClick(R.id.btn_inc_weight)
    public void incWeight() {
        etWeight.setText(String.valueOf(getTextFloat(etWeight) + MIN_FLOAT));
    }

    /* Decrease weight*/
    @OnClick(R.id.btn_dec_weight)
    public void decWeight() {
        etWeight.setText(String.valueOf(Math.max(getTextFloat(etWeight) - MIN_FLOAT, MIN_FLOAT)));
    }

    /* Adds exercise to exercises ArrayList and updates the exercises GridLayout display */
    @OnClick(R.id.btn_add_exercise)
    public void addExercise() {
        if (validateForm(this, formEntries)) {

            String exName = getTextString(etExerciseName);

            if (workout.containsExercise(exName)) {
                etExerciseName.setError(getString(R.string.err_exercise_exists));
            } else {
                exercises.add(updateExerciseData(exName));
                updateAdapter();
            }
        }
    }

    @OnClick(R.id.btn_update_exercise)
    public void updateExercise() {
        if (validateForm(this, formEntries)) {
            exercises.set(exercises.indexOf(ex), updateExerciseData(ex.getName()));
            updateAdapter();
        }
    }

    private Exercise updateExerciseData(String exName) {
        // add set objects matching the number of num_sets user chose
        exSets = new ArrayList<>();
        for (int i = 1; i <= getTextInt(etNumSets); i++) {
            exSets.add(new Set(
                    i,
                    getTextInt(etNumReps),
                    getTextFloat(etWeight)
            ));
        }

        etExerciseName.setText("");

        // add exercise to list
        return new Exercise(
                exName,
                sprType.getSelectedItem().toString(),
                sprEquipment.getSelectedItem().toString(),
                exSets
        );
    }


    /* Helper function to update the GridLayout exercises display */
    public void updateAdapter() {
        exerciseAdapter = new SingleItemAdapter(
                this,
                workout.getExerciseNames(),
                R.layout.part_square_button,
                R.id.sqrBtnListItem
        );
        exerciseAdapter.setItemClickObserver(this);
        rvExerciseList.setAdapter(exerciseAdapter);
    }


    /* Adds workout to exercises ArrayList and updates exercises GridLayout display */
    @OnClick(R.id.btn_add_workout)
    public void addWorkout() {
        if (validateForm(this, new EditText[]{etWorkoutName})) {

            if (exercises.isEmpty()) {
                Toast.makeText(this, "Error: No exercises added.", Toast.LENGTH_SHORT).show();
            } else {

                workout = new Workout(getTextString(etWorkoutName), exercises);
                addWorkoutFirebase(workout);

                discardWorkout();
            }
        }
    }

    /* Adds workout to exercises ArrayList and updates exercises GridLayout display */
    @OnClick(R.id.btn_clear_exercise)
    public void clearExercise() {
        clearFormEntry(etExerciseName);
        etNumReps.setText(String.valueOf(minInt));
        etNumSets.setText(String.valueOf(minInt));
        etWeight.setText(String.valueOf(minWeight));
        sprEquipment.setSelection(0);
        sprType.setSelection(0);
    }

    /* Adds workout to exercises ArrayList and updates exercises GridLayout display */
    @OnClick(R.id.btn_discard_workout)
    public void discardWorkout() {
        workoutHelper.close();
        finish();
    }

    @OnItemSelected(R.id.spr_type)
    public void changeSprEquipment(Spinner spr) {
        if (spr.getSelectedItemPosition() == POS_CARDIO) {
            sprEquipment.setSelection(POS_NA);
        }
    }

    @OnItemSelected(R.id.spr_equipment)
    public void changeSprType(Spinner spr) {
        Toast.makeText(this, spr.getItemAtPosition(POS_NA).toString(), Toast.LENGTH_SHORT).show();

        if (spr.getSelectedItemPosition() != POS_NA) {
            if (sprType.getSelectedItemPosition() == POS_CARDIO) {
                sprType.setSelection(POS_STREN);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
