package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Intent;
import android.database.DataSetObserver;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.*;

import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Workout;
import com.orhanobut.logger.Logger;
import org.parceler.Parcels;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.CALLING_ACTIVITY.*;
import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.*;
import static ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.*;
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

        setupPager();
    }

    WorkoutPagerAdapter  workoutPagerAdapter;

    ArrayList<TabLayout.Tab> tabs = new ArrayList<>();
    private void setupPager() {
        layInflater = getLayoutInflater();

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getText() == null) {
                    numExs++;
                    workoutPagerAdapter.addTab();

                    tab.getPosition();
                    tab.setText(String.format(TAB_LABEL, tab.getPosition() + 1));
                    tab.setIcon(null);
                    tabs.add(tab);
                    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus));

                    viewPager.setAdapter(workoutPagerAdapter);
                    viewPager.setCurrentItem(tab.getPosition());
                } else {
                    super.onTabSelected(tab);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
            }
        });

        for (int i = 0; i < this.numExs; i ++) {
            TabLayout.Tab tab = tabLayout.newTab().setText(String.format(TAB_LABEL, i + 1));
            tabs.add(tab);
            tabLayout.addTab(tab);
        }
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus));

        workoutPagerAdapter = new WorkoutPagerAdapter(getSupportFragmentManager(), this.numExs);
        workoutPagerAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                viewPager.setAdapter(workoutPagerAdapter);
            }
        });
        viewPager.setAdapter(workoutPagerAdapter);
        viewPager.setCurrentItem(0);
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

        Toast.makeText(this, "" + exercise.getExerciseNumber(), Toast.LENGTH_SHORT).show();

        if (workout.getNumExercises() == this.numExs) {
            Intent newWorkoutSummaryIntent = new Intent(this, NewWorkoutSummary.class);
            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout));
            newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, EXERCISES_ENTRY);
            startActivityForResult(newWorkoutSummaryIntent, REQ_NEW_WORKOUT_SUMMARY);
        } else {
            for (int i = 0; i < this.numExs; i++) {
                if (!workout.exerciseAtNumExists(i)) {
                    viewPager.setCurrentItem(i, true);
                    break;
                }
            }
        }
    }

    @Override
    public void deleteExercise(@Nullable Exercise exercise, int index) {
        if (exercise != null) {
            workout.removeExercise(exercise);
        }
        this.numExs--;

        tabLayout.removeTabAt(index);
        tabs.remove(index);

        if (index != 0) {
            tabs.get(index - 1).select();
        } else {
            tabs.get(0).select();
        }

        for (int i = 0; i < tabs.size(); i++) {
            TabLayout.Tab tab = tabs.get(i);
            tab.setText(String.format(TAB_LABEL, i + 1));
        }

        workoutPagerAdapter.removeTabFragment(index);
        workoutPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void cancelWorkout() {
        finish();
    }

}
