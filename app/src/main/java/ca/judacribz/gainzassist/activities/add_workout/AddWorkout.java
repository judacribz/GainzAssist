package ca.judacribz.gainzassist.activities.add_workout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.*;
import ca.judacribz.gainzassist.R;

import static ca.judacribz.gainzassist.util.UI.*;

public class AddWorkout extends AppCompatActivity{

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final int MIN_INT = 0;
    private static final float MIN_FLOAT = 0.0f;

    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    EditText etTmp;

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

        setTextWatcher(etSets, btnDecSets, true);
        setTextWatcher(etReps, btnDecReps, true);
        setTextWatcher(etWeight, btnDecWeight, false);


        setSpinnerWithArray(this, R.array.exerciseType, sprExerciseType);
        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipmentUsed);
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
                    et.setText("0");
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
    @OnClick({R.id.btn_inc_reps, R.id.btn_dec_reps, R.id.btn_inc_sets, R.id.btn_dec_sets})
    public void changeInt(ImageButton iBtn) {
        int intChange = 0;

        switch(iBtn.getId()) {
            case R.id.btn_inc_sets:
                intChange = 1;
                etTmp = etSets;
                break;

            case R.id.btn_dec_sets:
                intChange = -1;
                etTmp = etSets;
                break;

            case R.id.btn_inc_reps:
                intChange = 1;
                etTmp = etReps;
                break;

            case R.id.btn_dec_reps:
                intChange = -1;
                etTmp = etReps;
                break;
        }

        int tmpInt = Math.max(getInt(etTmp) + intChange, MIN_INT);
        etTmp.setText(String.valueOf(tmpInt));
    }

    @OnClick({R.id.btn_inc_weight, R.id.btn_dec_weight})
    public void changeFloat(ImageButton iBtn) {
        float floatChange = 5.0f;

        etTmp = etWeight;
        switch(iBtn.getId()) {
            case R.id.btn_inc_weight:
                floatChange = 5.0f;
                break;

            case R.id.btn_dec_weight:
                floatChange = -5.0f;
                break;
        }
        float tmpFloat = Math.max(getFloat(etTmp) + floatChange, MIN_FLOAT);
        etTmp.setText(String.valueOf(tmpFloat));
    }


    @OnClick({R.id.btn_discard_exercise, R.id.btn_add_exercise, R.id.btn_discard_workout, R.id.btn_add_workout})
    public void discSets() {
        Toast.makeText(this, "yo", Toast.LENGTH_SHORT).show();
    }
    // ============================================================================================
}
