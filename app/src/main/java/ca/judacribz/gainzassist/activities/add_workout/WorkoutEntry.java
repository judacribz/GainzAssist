package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import butterknife.*;
import ca.judacribz.gainzassist.R;

import static ca.judacribz.gainzassist.util.UI.*;

public class WorkoutEntry extends AppCompatActivity{

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_WORKOUT_NAME
            = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_WORKOUT_NAME";
    public static final String EXTRA_NUM_EXERCISES
            = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_NUM_EXERCISES";
    public static final int MIN_NUM_EXERCISES = 1;
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
        numExs = (numExStr.isEmpty()) ? MIN_NUM_EXERCISES : Integer.valueOf(numExStr);

        if (numExs <= MIN_NUM_EXERCISES) {
            ibtnDecExercises.setEnabled(false);
            ibtnDecExercises.setVisibility(View.GONE);

            if (numExs < MIN_NUM_EXERCISES)
                etNumExercises.setText(String.valueOf(MIN_NUM_EXERCISES));
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
    /* Increase number of sets */
    @OnClick(R.id.ibtn_inc_exercises)
    public void incNumExs() {
        etNumExercises.setText(String.valueOf(getTextInt(etNumExercises) + 1));
    }

    /* Decrease number of sets */
    @OnClick(R.id.ibtn_dec_exercises)
    public void decNumExs() {
        etNumExercises.setText(String.valueOf(getTextInt(etNumExercises) - 1));
    }

    @OnClick(R.id.btn_enter)
    public void enterWorkoutName() {
        Intent intent = new Intent(this, ExercisesEntry.class);

        if (!isEmpty) {
            intent.putExtra(EXTRA_WORKOUT_NAME, getTextString(etWorkoutName));
        }


        intent.putExtra(EXTRA_NUM_EXERCISES, getTextInt(etNumExercises));
        startActivity(intent);
    }

    @OnClick(R.id.btn_cancel)
    public void cancelWorkout() {
        finish();
    }
    //=Click=Handling===============================================================================

}