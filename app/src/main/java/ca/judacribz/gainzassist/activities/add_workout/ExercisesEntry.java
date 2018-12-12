package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.CurrWorkout;
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Workout;
import com.orhanobut.logger.Logger;
import org.parceler.Parcels;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.CALLING_ACTIVITY.*;
import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.*;
import static ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.*;
import static ca.judacribz.gainzassist.activities.start_workout.CurrWorkout.*;
import static ca.judacribz.gainzassist.util.UI.*;

public class ExercisesEntry extends AppCompatActivity implements ExEntry.ExEntryDataListener {

    // Constants
    // --------------------------------------------------------------------------------------------

    public static final int REQ_NEW_WORKOUT_SUMMARY = 1002;
    public static final String TAB_LABEL = "Exercise %s";
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    Workout workout;
    int numExs;
    ArrayList<Integer> exInds = new ArrayList<>();

    LayoutInflater layInflater;

    @BindView(R.id.tlay_navbar) TabLayout tabLayout;
    @BindView(R.id.vp_fmt_container) ViewPager viewPager;
    // --------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(
                this,
                R.layout.activity_exercises_entry,
                "Exercises Entry",
                true
        );
        this.workout = new Workout();

        Intent workoutEntryIntent = getIntent();
        this.workout.setName(workoutEntryIntent.getStringExtra(EXTRA_WORKOUT_NAME));
        this.numExs = workoutEntryIntent.getIntExtra(EXTRA_NUM_EXERCISES, MIN_NUM);
        for (int i = 0; i < this.numExs; i++) {
            this.exInds.add(i);
        }

        setupPager();
    }

    WorkoutPagerAdapter  workoutPagerAdapter;

    private void setupPager() {
        layInflater = getLayoutInflater();

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

            Drawable icon;
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getText() == null) {
                    tab.getPosition();
                    workoutPagerAdapter.addTab();
                    tab.setText(String.format(TAB_LABEL, tab.getPosition() + 1));
                    tab.setIcon(null);
                    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus));

                    viewPager.setAdapter(workoutPagerAdapter);
                    viewPager.setCurrentItem(tab.getPosition());
                } else {

                    super.onTabSelected(tab);
                }
//                icon = tab.getIcon();
//                if (icon != null) {
//                    icon.setColorFilter(
//                            ContextCompat.getColor(getApplicationContext(), R.color.colorBg),
//                            PorterDuff.Mode.SRC_IN
//                    );
//                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);

//                icon = tab.getIcon();
//                if (icon != null) {
//                    icon.setColorFilter(
//                            ContextCompat.getColor(getApplicationContext(), R.color.colorGreen),
//                            PorterDuff.Mode.SRC_IN
//                    );
//                }
            }
        });


        for (int i = 0; i < this.numExs; i ++) {
            tabLayout.addTab(tabLayout.newTab().setText(String.format(TAB_LABEL, i + 1)));
        }
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus));

        workoutPagerAdapter = new WorkoutPagerAdapter(getSupportFragmentManager(), this.numExs);
        viewPager.setAdapter(workoutPagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(this.numExs + 1);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == REQ_NEW_WORKOUT_SUMMARY) {
            finish();
        }
    }

    @Override
    public boolean checkExerciseExists(ExEntry fmt, String exerciseName) {
        if (workout.containsExercise(exerciseName)) {
            fmt.setExerciseExists();
            return true;
        }

        return false;
    }

    @Override
    public void exerciseDataReceived(Exercise exercise) {
        workout.addExercise(exercise);

        Logger.d(exInds);
        Toast.makeText(this, "" + exercise.getExerciseNumber(), Toast.LENGTH_SHORT).show();
        exInds.remove((Integer) exercise.getExerciseNumber());
        Logger.d(exInds);

        if (exInds.size() == 0) {
            Intent newWorkoutSummaryIntent = new Intent(this, NewWorkoutSummary.class);
            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout));
            newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, EXERCISES_ENTRY);
            startActivityForResult(newWorkoutSummaryIntent, REQ_NEW_WORKOUT_SUMMARY);
        } else {
            viewPager.setCurrentItem(exInds.get(0), true);
        }
    }

    @Override
    public void cancelWorkout() {
        finish();
    }

}
