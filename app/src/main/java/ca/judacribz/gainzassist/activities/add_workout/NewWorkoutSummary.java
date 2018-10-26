package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.WorkoutHelper;

import static ca.judacribz.gainzassist.activities.add_workout.ExercisesEntry.EXTRA_WORKOUT;
import static ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.EXTRA_WORKOUT_NAME;
import static ca.judacribz.gainzassist.firebase.Database.addWorkoutFirebase;
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
    ArrayList<String> exerciseNames;

    ArrayList<Exercise> exercises;
    ArrayList<Set> exSets;
    Workout workout;

    EditText[] forms;
    WorkoutHelper workoutHelper;

    // UI Elements
    @BindView(R.id.et_workout_name) EditText etWorkoutName;

    @BindView(R.id.et_exercise_name) EditText etExerciseName;
    @BindView(R.id.spr_type) Spinner sprType;
    @BindView(R.id.spr_equipment) Spinner sprEquipment;
    @BindView(R.id.et_reps) EditText etReps;
    @BindView(R.id.et_weight) EditText etWeight;
    @BindView(R.id.et_sets) EditText etSets;

    @BindView(R.id.btn_dec_reps) ImageButton btnDecReps;
    @BindView(R.id.btn_dec_weight) ImageButton btnDecWeight;
    @BindView(R.id.btn_dec_sets) ImageButton btnDecSets;

    @BindView(R.id.rv_exercise_btns) RecyclerView rvExerciseList;
    // --------------------------------------------------------------------------------------------


    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workout_summary);
        ButterKnife.bind(this);
        setToolbar(this, R.string.create_workout, true);

        Intent workoutEntryIntent = getIntent();
        workout = workoutEntryIntent.getParcelableExtra(EXTRA_WORKOUT);

        setSpinnerWithArray(this, R.array.exerciseType, sprType);
        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipment);

        setTextWatcher(etSets, btnDecSets, true);
        setTextWatcher(etReps, btnDecReps, true);
        setTextWatcher(etWeight, btnDecWeight, false);

        // Set the layout manager
        rvExerciseList.setLayoutManager(new GridLayoutManager(
                this,
                getNumColumns(this),
                GridLayoutManager.HORIZONTAL,
                false
        ));
        rvExerciseList.setHasFixedSize(true);

        forms = new EditText[]{etExerciseName, etReps, etWeight, etSets};
        exerciseNames = new ArrayList<>();
        exercises = workout.getExercises();
        for (Exercise exercise : exercises) {
            exerciseNames.add(exercise.getName());
        }
        updateAdapter();

        etWorkoutName.setText(workout.getName());

        workoutHelper = new WorkoutHelper(this);
    }

    /* Disables decrease ImageButtons when EditText value is at 0 (for reps or num_sets) */
    public void setTextWatcher(final EditText et, final ImageButton btnDec, final boolean isInt) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strTmp = s.toString();
                boolean isZero = false;

                if (!strTmp.isEmpty()) {
                    if (isInt) {
                        if (Integer.valueOf(strTmp) == MIN_INT) {
                            isZero = true;
                        }
                    } else {
                        if (Float.valueOf(strTmp) == MIN_FLOAT) {
                            isZero = true;
                        }
                    }

                    if (isZero) {
                        btnDec.setEnabled(false);
                    } else {
                        if (!btnDec.isEnabled()) {
                            btnDec.setEnabled(true);
                        }
                    }

                } else {
                    if (isInt) {
                        et.setText(String.valueOf(MIN_INT));
                    } else {
                        et.setText(String.valueOf(MIN_FLOAT));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /* Toolbar back arrow handling */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////


    // SingleItemAdapter.ItemClickObserver override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onWorkoutClick(String name) {
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWorkoutLongClick(View anchor, String name) {

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Click Handling
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Increase number of reps */
    @OnClick(R.id.btn_inc_reps)
    public void incReps() {
        etReps.setText(String.valueOf(getTextInt(etReps) + MIN_INT));
    }

    /* Decrease number of reps */
    @OnClick(R.id.btn_dec_reps)
    public void decReps() {
        etReps.setText(String.valueOf(Math.max(getTextInt(etReps) - MIN_INT, MIN_INT)));
    }

    /* Increase number of num_sets */
    @OnClick(R.id.btn_inc_sets)
    public void incSets() {
        etSets.setText(String.valueOf(getTextInt(etSets) + MIN_INT));
    }

    /* Decrease number of num_sets */
    @OnClick(R.id.btn_dec_sets)
    public void decSets() {
        etSets.setText(String.valueOf(Math.max(getTextInt(etSets) - MIN_INT, MIN_INT)));
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
        if (validateForm(this, forms)) {

            String exName = getTextString(etExerciseName);

            if (exerciseNames.contains(exName)) {
                etExerciseName.setError(getString(R.string.err_exercise_exists));
            } else {
                exerciseNames.add(exName);
                updateAdapter();

                // add set objects matching the number of num_sets user chose
                exSets = new ArrayList<>();
                for (int i = 1; i <= getTextInt(etSets); i++) {
                    exSets.add(new Set(
                            i,
                            getTextInt(etReps),
                            getTextFloat(etWeight)
                    ));
                }

                // add exercise to list
                exercises.add(new Exercise(
                        exName,
                        sprType.getSelectedItem().toString(),
                        sprEquipment.getSelectedItem().toString(),
                        exSets
                ));

                etExerciseName.setText("");
            }
        }
    }

    /* Helper function to update the GridLayout exercises display */
    public void updateAdapter() {
        exerciseAdapter = new SingleItemAdapter(
                this,
                exerciseNames,
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
        clearForm(forms);
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
