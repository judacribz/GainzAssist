package ca.judacribz.gainzassist.activity_add_workout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.*;
import ca.judacribz.gainzassist.R;
import static ca.judacribz.gainzassist.utilities.UserInterface.setToolbar;

public class AddWorkout extends AppCompatActivity implements View.OnClickListener {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @BindView(R.id.et_workout_name) EditText etWorkoutName;
    @BindView(R.id.et_exercise_name) EditText etExerciseName;
    @BindView(R.id.et_reps) EditText etReps;
    @BindView(R.id.et_weight) EditText etWeight;
    @BindView(R.id.et_sets) EditText etSets;

    @BindView(R.id.spr_type) Spinner sprExerciseType;
    @BindView(R.id.spr_equipment) Spinner sprEquipmentUsed;
    @BindView(R.id.rv_exercise_btns) RecyclerView rvExerciseList;

    @BindView(R.id.btn_inc_reps) ImageButton btnIncReps;
    @BindView(R.id.btn_dec_reps) ImageButton btnDecReps;
    @BindView(R.id.btn_inc_weight) ImageButton btnIncWeight;
    @BindView(R.id.btn_dec_weight) ImageButton btnDecWeight;
    @BindView(R.id.btn_inc_sets) ImageButton btnIncSets;
    @BindView(R.id.btn_dec_sets) ImageButton btnDecSets;

    @BindView(R.id.btn_discard_exercise) Button btnDiscardExercise;
    @BindView(R.id.btn_add_exercise) Button btnAddExercise;
    @BindView(R.id.btn_discard_workout) Button btnDiscardWorkout;
    @BindView(R.id.btn_add_workout) Button btnAddWorkout;
    // --------------------------------------------------------------------------------------------


    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);
        ButterKnife.bind(this);
        setToolbar(this, R.string.create_workout, true);

        btnIncReps.setOnClickListener(this);
        btnDecReps.setOnClickListener(this);
        btnIncWeight.setOnClickListener(this);
        btnDecWeight.setOnClickListener(this);
        btnIncSets.setOnClickListener(this);
        btnDecSets.setOnClickListener(this);

        btnDiscardExercise.setOnClickListener(this);
        btnAddExercise.setOnClickListener(this);
        btnDiscardWorkout.setOnClickListener(this);
        btnAddWorkout.setOnClickListener(this);

        ArrayAdapter<CharSequence> exTypeAdapter = ArrayAdapter.createFromResource(this, R.array.exerciseType, android.R.layout.simple_spinner_item);
        exTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprExerciseType.setAdapter(exTypeAdapter);
        ArrayAdapter<CharSequence> exEquipAdapter = ArrayAdapter.createFromResource(this, R.array.exerciseEquipment, android.R.layout.simple_spinner_item);
        exEquipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprEquipmentUsed.setAdapter(exEquipAdapter);
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
    @Override
    public void onClick(View v) {
        handleClick(v.getId());
    }

    /* Handles all clicks in activity */
    public void handleClick(int id) {
        switch (id) {
            case R.id.btn_discard_exercise:
                Toast.makeText(this, "discEx", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_add_exercise:
                Toast.makeText(this, "ex", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_discard_workout:
                Toast.makeText(this, "discWork", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_add_workout:
                Toast.makeText(this, "r", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    // ============================================================================================
}
