package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.Toast;
import butterknife.*;
import ca.judacribz.gainzassist.R;

import static ca.judacribz.gainzassist.util.UI.*;

public class WorkoutEntry extends AppCompatActivity{

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_WORKOUT_NAME
            = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_WORKOUT";
    public static final String EXTRA_NUM_EXERCISES
            = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_NUM_EXERCISES";

    public static final int REQ_EXERCISE_ENTRY = 1001;

    public static final int MIN_NUM = 1;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    boolean isEmpty = true;
    int numExs;

    @BindView(R.id.et_workout_name) EditText etWorkoutName;
    @BindView(R.id.et_num_exercises) EditText etNumExercises;
    @BindView(R.id.btn_enter) Button btnEnter;
    @BindView(R.id.ibtn_inc_exercises) ImageButton ibtnIncExercises;
    @BindView(R.id.ibtn_dec_exercises) ImageButton ibtnDecExercises;
    // --------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, R.layout.activity_workout_entry, R.string.add_workout, true);

    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == REQ_EXERCISE_ENTRY) {
                finish();
        }
    }

    /* Toolbar back arrow handling */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }


    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(value = R.id.et_num_exercises, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    public void beforeNumExercisesChanged() {
        if (!ibtnDecExercises.isEnabled()) {
            ibtnDecExercises.setEnabled(true);
            ibtnDecExercises.setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(value = R.id.et_num_exercises, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onNumExercisesChanged(CharSequence s, int start, int before, int count) {
        String numExStr = s.toString();
        numExs = (numExStr.isEmpty()) ? MIN_NUM : Integer.valueOf(numExStr);

        if (numExs <= MIN_NUM) {
            ibtnDecExercises.setEnabled(false);
            ibtnDecExercises.setVisibility(View.GONE);

            if (numExs < MIN_NUM)
                etNumExercises.setText(String.valueOf(MIN_NUM));
        }

    }

    @OnTextChanged(value = R.id.et_workout_name, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onWorkoutNameChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim().isEmpty()) {
            if (!isEmpty) {
                isEmpty = true;
                btnEnter.setText(R.string.skip);
            }
        } else {
            if (isEmpty) {
                isEmpty = false;
                btnEnter.setText(R.string.enter);
            }
        }
    }
    // =TextWatcher=Handling========================================================================


    // Click Handling
    // =============================================================================================
    /* Increase number of num_sets */
    @OnClick(R.id.ibtn_inc_exercises)
    public void incNumExs() {
        etNumExercises.setText(String.valueOf(getTextInt(etNumExercises) + 1));
    }

    /* Decrease number of num_sets */
    @OnClick(R.id.ibtn_dec_exercises)
    public void decNumExs() {
        etNumExercises.setText(String.valueOf(getTextInt(etNumExercises) - 1));
    }

    @OnClick(R.id.btn_enter)
    public void enterWorkoutName() {
        if (validateFormEntry(this, etNumExercises)) {
            Intent exercisesEntry = new Intent(this, ExercisesEntry.class);

            if (!isEmpty) {
                Toast.makeText(this, "" + getTextString(etWorkoutName), Toast.LENGTH_SHORT).show();
                exercisesEntry.putExtra(EXTRA_WORKOUT_NAME, getTextString(etWorkoutName));
            }


            exercisesEntry.putExtra(EXTRA_NUM_EXERCISES, getTextInt(etNumExercises));
            startActivityForResult(exercisesEntry, REQ_EXERCISE_ENTRY);
        }
    }

    @OnClick(R.id.btn_cancel)
    public void cancelWorkout() {
        finish();
    }
    //=Click=Handling===============================================================================

}