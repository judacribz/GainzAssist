package ca.judacribz.gainzassist.activities.add_workout;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Workout;
import com.orhanobut.logger.Logger;
import org.parceler.Parcels;
import java.util.ArrayList;

import static ca.judacribz.gainzassist.constants.ExerciseConst.*;
import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.CALLING_ACTIVITY.*;
import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.*;
import static ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.*;
import static ca.judacribz.gainzassist.util.Misc.shrinkTo;
import static ca.judacribz.gainzassist.util.UI.*;

public class ExercisesEntry extends AppCompatActivity implements ExEntry.ExEntryDataListener {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final int REQ_NEW_WORKOUT_SUMMARY = 1002;
    public static final String TAB_LABEL = "Exercise %s";
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    WorkoutPagerAdapter  workoutPagerAdapter;
    TabLayout.TabLayoutOnPageChangeListener tabLayoutOnPageChangeListener;
    TabLayout.ViewPagerOnTabSelectedListener viewPagerOnTabSelectedListener;
    ArrayList<TabLayout.Tab> tabs = new ArrayList<>();
    LayoutInflater layInflater;

    Workout workout;
    long workoutId;
    int
            pos,
            numExs,
            addedExs = 0;

    ArrayList<Exercise> exercises;

    @BindView(R.id.tlay_navbar) TabLayout tabLayout;
    @BindView(R.id.vp_fmt_container) ViewPager viewPager;
    // --------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(
                this,
                R.layout.activity_exercises_entry,
                "Exercises Entry",
                true
        );


        workout = new Workout();
        workout.setId(-1);
        workoutId = workout.getId();

        Intent workoutEntryIntent = getIntent();
        workout.setName(workoutEntryIntent.getStringExtra(EXTRA_WORKOUT_NAME));
        numExs = workoutEntryIntent.getIntExtra(EXTRA_NUM_EXERCISES, MIN_INT);

        exercises = new ArrayList<>();
        for (int i = 0; i < numExs; i ++) {
            exercises.add(new Exercise());
        }


        tabLayoutOnPageChangeListener = new TabLayout.TabLayoutOnPageChangeListener(tabLayout);
        viewPagerOnTabSelectedListener = new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);

                if (tab.getText() == null) {
                    numExs++;
                    exercises.add(new Exercise());
                    workoutPagerAdapter.addTab();
                    workoutPagerAdapter.notifyDataSetChanged();

                    pos = tab.getPosition();
                    tab.setText(String.format(TAB_LABEL, pos + 1));
                    tab.setIcon(null);
                    tabs.add(tab);

                    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus));
                    viewPager.setCurrentItem(numExs - 1, true);
                    tab.select();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
            }
        };

        setupPager();
    }

    private void setupPager() {
        layInflater = getLayoutInflater();

        viewPager.addOnPageChangeListener(tabLayoutOnPageChangeListener);
        tabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedListener);

        for (int i = 0; i < this.numExs; i++) {
            TabLayout.Tab tab = tabLayout.newTab().setText(String.format(TAB_LABEL, i + 1));
            tabs.add(tab);
            tabLayout.addTab(tab);
        }
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus));

        viewPager.setAdapter(workoutPagerAdapter =
                new WorkoutPagerAdapter(getSupportFragmentManager(), this.numExs));
        viewPager.setCurrentItem(0);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == REQ_NEW_WORKOUT_SUMMARY) {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////


    // ExEntry.ExEntryDataListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean exerciseDoesNotExist(ExEntry fmt, String exerciseName, int skipIndex) {
        String name;

        for (Exercise ex : exercises) {
            if (skipIndex == exercises.indexOf(ex)) {
                continue;
            }

            if ((name = ex.getName()) != null) {
                if (name.equals(exerciseName)) {
                    fmt.setExerciseExists();

                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void exerciseDataReceived(Exercise exercise, boolean update) {
        exercise.setWorkoutId(workoutId);
        exercises.set(exercise.getExerciseNumber(), exercise);

        if (!update) {
            addedExs++;
        }

        lognow();
        if (!checkAndGoToSummary()) {
            setFirstEmptyTab();
        }
    }

    public void setFirstEmptyTab() {
        for (int i = 0; i < numExs; i++) {

            if (exercises.get(i).getName() == null) {
                viewPager.setCurrentItem(i, true);

                break;
            }
        }
    }

    private void lognow() {
        Exercise e;
        for (int i = 0; i < exercises.size(); i++) {
            e = exercises.get(i);
            String g;
            if (e == null) {
                g = "null";
            } else {
                g = e.getName();
            }
            Logger.d("INDEX = " + (i + 1) + " value=  " + g);
        }
        Logger.d("INDEX = -----------------------------------");
    }


    @Override
    public void deleteExercise(@Nullable Exercise exercise, int index) {
        if (exercise != null) {
            addedExs--;
        }
        this.numExs--;

        exercises.remove(index);
        shrinkTo(exercises, numExs);

        lognow();

        tabLayout.removeTabAt(index);
        tabs.remove(index);
        if (index != 0) {
            index --;
        }
        tabs.get(index).select();

        for (int i = index; i < tabs.size(); i++) {
            TabLayout.Tab tab = tabs.get(i);
            tab.setText(String.format(TAB_LABEL, i + 1));
        }

        workoutPagerAdapter.removeFragment(index, exercises);
        workoutPagerAdapter.notifyDataSetChanged();

        checkAndGoToSummary();

        if (this.numExs <= 1) {
            workoutPagerAdapter.hideDelete();
        }
    }

    //ExEntry.ExEntryDataListener//Override////////////////////////////////////////////////////////

    private boolean checkAndGoToSummary() {
        if (addedExs >= numExs) {
            workout.setExercises(exercises);

            Intent newWorkoutSummaryIntent = new Intent(this, NewWorkoutSummary.class);
            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout));
            newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, EXERCISES_ENTRY);
            startActivityForResult(newWorkoutSummaryIntent, REQ_NEW_WORKOUT_SUMMARY);

            return  true;
        }

        return false;
    }
}
