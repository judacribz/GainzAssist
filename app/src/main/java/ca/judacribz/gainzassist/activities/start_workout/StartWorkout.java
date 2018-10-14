package ca.judacribz.gainzassist.activities.start_workout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.*;
import java.util.ArrayList;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.models.CurrSet;
import ca.judacribz.gainzassist.models.CurrUser;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.WorkoutHelper;

import static ca.judacribz.gainzassist.util.Calculations.getOneRepMax;
import static ca.judacribz.gainzassist.util.UI.*;
import static ca.judacribz.gainzassist.activities.workouts_list.WorkoutsList.EXTRA_WORKOUT_NAME;

public class StartWorkout extends AppCompatActivity {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    public Activity act;
    public WorkoutHelper workoutHelper;
    public static Workout workout;
    View setsView;
    LayoutInflater layInflater;

    TextView tvExerciseName;
    RecyclerView setList;

    @BindView(R.id.tlay_navbar) TabLayout tabLayout;
    @BindView(R.id.vp_fmt_container) ViewPager viewPager;
    // --------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, R.layout.activity_start_workout, getIntent().getStringExtra(EXTRA_WORKOUT_NAME), true);
        setTheme(R.style.WorkoutTheme);

        act = this;

        workoutHelper = new WorkoutHelper(this);
        workout = workoutHelper.getWorkout(getIntent().getStringExtra(EXTRA_WORKOUT_NAME));

        layInflater = getLayoutInflater();

        genWarmups();
        setupPager();
    }

    /* Setup fragments with page with icons for the tab bar */
    private void setupPager() {
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

            Drawable icon;
            @Override
            public void onTabSelected(Tab tab) {
                super.onTabSelected(tab);
                icon = tab.getIcon();
                if (icon != null) {
                    icon.setColorFilter(
                            ContextCompat.getColor(getApplicationContext(), R.color.colorTitle),
                            PorterDuff.Mode.SRC_IN
                    );
                }
            }

            @Override
            public void onTabUnselected(Tab tab) {
                super.onTabUnselected(tab);

                icon = tab.getIcon();
                if (icon != null) {
                    icon.setColorFilter(
                            ContextCompat.getColor(getApplicationContext(), R.color.colorText),
                            PorterDuff.Mode.SRC_IN
                    );
                }
            }
        });

        viewPager.setAdapter(new WorkoutPagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(3);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStop() {
        super.onStop();
        CurrUser.getInstance().clearWarmups();
    }

    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////

    /* Creates horizontal recycler view lists of  set#, reps, weights for each exercise and adds
     * dynamically to the view.
     * Called in CurrExercises and CurrWarmups
     */
    @SuppressLint("InflateParams")
    public void displaySets(int id,
                            String exerciseName,
                            ArrayList<Set> sets,
                            LinearLayout vgSubtitle,
                            LinearLayout vgSets) {

        // Add subtitle layout which includes "Set #", "Reps" and "Weight"
        setsView = layInflater.inflate(R.layout.part_sets_subtitles, null);
        vgSubtitle.addView(setsView, 0);

        // Add the listView layout which contains a textView and a recyclerVIew
        setsView = layInflater.inflate(R.layout.part_horizontal_rv, null);
        setsView.setId(id);
        vgSets.addView(setsView, 0);

        // Set the title in the textView within the listView layout above
        tvExerciseName = (TextView) setsView.findViewById(R.id.tv_exercise_name);
        tvExerciseName.setText(exerciseName);

        // Set the recyclerView list to be horizontal and pass in the exercise sets through the
        // adapter
        setList = (RecyclerView) setsView.findViewById(R.id.rv_exercise_sets);
        setList.setHasFixedSize(true);
        setList.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false));
        setList.setAdapter(new SetsAdapter(sets));
    }

    // TODO optimize
    public void genWarmups() {
        ArrayList<Exercise> exercises = workout.getExercises();
        ArrayList<Exercise> warmups = new ArrayList<>();
        ArrayList<Set> sets = new ArrayList<>();

        float oneRepMax, minWeight, weight, percWeight, newWeight;
        int reps, setNum = 1;
        String equip;

        for (Exercise exercise: exercises) {
            equip = exercise.getEquipment();
            minWeight = getString(R.string.barbell).equals(equip.toLowerCase()) ? 45f : 0f;
            weight = exercise.getAvgWeight();
            if (weight == minWeight)
                continue;

            oneRepMax = getOneRepMax(exercise.getAvgReps(), exercise.getAvgWeight());
            reps = exercise.getAvgReps();

            percWeight = minWeight/weight;
            sets.add(new Set(setNum++, reps, minWeight));
            do {
                newWeight = percWeight*weight;
                newWeight -= newWeight % 5;
                sets.add(new Set(setNum++, reps, newWeight));

                percWeight += 0.2f;
                if (reps - 2 > 0)
                    reps -= 2;
                else if (reps - 1 > 0)
                    --reps;

//            while (weightAmt > minWeight && weightAmt < weight) {
//                set = sets.get(j);
//                weightAmt = (int) (percent * weight);
//                weightAmt -= weightAmt % 5;
//                Log.d("START_WORKOUT", "genWarmups: " + minWeight);
//                set.setWeight(Math.max((float) weightAmt, minWeight));
//                sets.set(j, set);
//                percent += increments;
//            }

            } while (percWeight < 0.8f);

            warmups.add(new Exercise(exercise.getName(), exercise.getType(), exercise.getEquipment(), sets));
        }

        CurrUser.getInstance().setWarmups(warmups);
    }
}
