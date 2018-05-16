package ca.judacribz.gainzassist.activities.add_workout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.models.WorkoutHelper;

import static ca.judacribz.gainzassist.util.Calculations.getNumColumns;
import static ca.judacribz.gainzassist.util.UI.*;

public class AddWorkout extends AppCompatActivity implements SingleItemAdapter.ItemClickObserver{

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final int MIN_INT = 1;
    private static final float MIN_FLOAT = 5.0f;

    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    EditText etTmp;

    SingleItemAdapter workoutAdapter;
    LinearLayoutManager layoutManager;

    ArrayList<String> workoutNames;
    ArrayList<String> filteredWorkouts;
    WorkoutHelper workoutHelper;
    EditText[] forms = new EditText[5];

    @BindView(R.id.et_workout_name) EditText etWorkoutName;
    @BindView(R.id.et_exercise_name) EditText etExerciseName;
    @BindView(R.id.et_reps) EditText etReps;
    @BindView(R.id.et_weight) EditText etWeight;
    @BindView(R.id.et_sets) EditText etSets;

    @BindView(R.id.btn_dec_reps) ImageButton btnDecReps;
    @BindView(R.id.btn_dec_weight) ImageButton btnDecWeight;
    @BindView(R.id.btn_dec_sets) ImageButton btnDecSets;

    @BindView(R.id.spr_type) Spinner sprExerciseType;
    @BindView(R.id.spr_equipment) Spinner sprEquipmentUsed;
    @BindView(R.id.rv_exercise_btns) RecyclerView rvExerciseList;
    // --------------------------------------------------------------------------------------------


    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);
        ButterKnife.bind(this);
        setToolbar(this, R.string.create_workout, true);

        forms = new EditText[]{etWorkoutName, etExerciseName, etReps, etWeight, etSets};

        setTextWatcher(etSets, btnDecSets, true);
        setTextWatcher(etReps, btnDecReps, true);
        setTextWatcher(etWeight, btnDecWeight, false);


        setSpinnerWithArray(this, R.array.exerciseType, sprExerciseType);
        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipmentUsed);

        // Set the layout manager for the localWorkouts
        workoutNames = new ArrayList<>();

        // Set the layout manager for the localWorkouts
        layoutManager = new LinearLayoutManager(this);

        rvExerciseList.setLayoutManager(new GridLayoutManager(this, getNumColumns(this)));
        rvExerciseList.setHasFixedSize(true);

        // Get all workouts from database
        workoutHelper = new WorkoutHelper(this);
        workoutNames = workoutHelper.getAllWorkoutNames();
        workoutNames.add("WHAT??");
        workoutNames.add("YO??");
        workoutNames.add("YO??");
        workoutNames.add("YO??");
        workoutNames.add("YO??");

        workoutAdapter = new SingleItemAdapter(this, workoutNames, R.layout.list_item_square_button, R.id.sqrBtnListItem);
        workoutAdapter.setItemClickObserver(this);
        rvExerciseList.setAdapter(workoutAdapter);
    }

    /* Disables decrease ImageButtons when EditText value is at 0 (for reps or sets) */
    public void setTextWatcher(final EditText et, final ImageButton btnDec, final boolean isInt) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strTmp = s.toString();
                if (!strTmp.isEmpty()) {

                    boolean isZero = false;

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
                    et.setText(String.valueOf(MIN_INT));
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


    // Click Handling
    // ============================================================================================
    @OnClick(R.id.btn_inc_reps)
    public void incReps() {
        etReps.setText(String.valueOf(getInt(etReps) + 1));
    }

    @OnClick(R.id.btn_dec_reps)
    public void decReps() {
        etReps.setText(String.valueOf(Math.max(getInt(etReps) - 1, MIN_INT)));
    }

    @OnClick(R.id.btn_inc_sets)
    public void incSets() {
        etSets.setText(String.valueOf(getInt(etSets) + 1));
    }

    @OnClick(R.id.btn_dec_sets)
    public void decSets() {
        etSets.setText(String.valueOf(Math.max(getInt(etSets) - 1, MIN_INT)));
    }

    @OnClick(R.id.btn_inc_weight)
    public void incWeight() {
        etWeight.setText(String.valueOf(getFloat(etWeight) + 1));
    }

    @OnClick(R.id.btn_dec_weight)
    public void decWeight() {
        etWeight.setText(String.valueOf(Math.max(getFloat(etWeight) - 1, MIN_FLOAT)));
    }

    @OnClick(R.id.btn_add_exercise)
    public void addExercise() {
        if (validateForm(this, new EditText[]{etExerciseName})) {

        }
    }

    @OnClick({R.id.btn_discard_exercise, R.id.btn_add_exercise, R.id.btn_discard_workout, R.id.btn_add_workout})
    public void discSets() {
        Toast.makeText(this, "yo", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWorkoutClick(String name) {
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
    }
    // ============================================================================================
}
