package ca.judacribz.gainzassist.activities.start_workout;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.*;
import java.util.ArrayList;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.models.*;
import org.parceler.Parcels;

import static ca.judacribz.gainzassist.util.UI.*;
import static ca.judacribz.gainzassist.activities.workouts_list.WorkoutsList.EXTRA_WORKOUT;

public class StartWorkout extends AppCompatActivity {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    WorkoutHelper workoutHelper;
    Workout workout;
    ArrayList<Exercise> warmups, mainExercises;

    LayoutInflater layInflater;
    View setsView;

    CurrWorkout currWorkout;

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
        Intent intent = getIntent();
        workout = (Workout) Parcels.unwrap(intent.getParcelableExtra(EXTRA_WORKOUT));
        mainExercises = workout.getExercises();


        setInitView(this, R.layout.activity_start_workout, workout.getName(), true);
        setTheme(R.style.WorkoutTheme);

        currWorkout = CurrWorkout.getInstance();
        currWorkout.setCurrWorkout(workout);

        setupPager();

    }

    /* Setup fragments with page with icons for the tab bar */
    private void setupPager() {
        layInflater = getLayoutInflater();

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

        viewPager.setAdapter(new WorkoutPagerAdapter(
                getSupportFragmentManager(),
                mainExercises,
                currWorkout.getWarmups())
        );
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

        currWorkout.reset();
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////


    /* Creates horizontal recycler view lists of  set#, reps, weights for each exercise and adds
     * dynamically to the view.
     * Called in ExercisesList and WarmupsList
     */

    public void displaySets(int id,
                            Exercise exercise,
                            LinearLayout llSubtitle,
                            LinearLayout llSets) {

        // Add subtitle layout which includes "Set #", "Reps" and "Weight"
        setsView = layInflater.inflate(R.layout.part_sets_subtitles, null);
        llSubtitle.addView(setsView, 0);

        // Add the listView layout which containsExercise a textView and a recyclerVIew
        setsView = layInflater.inflate(R.layout.part_horizontal_rv, null);
        setsView.setId(id);
        llSets.addView(setsView, 0);

        // Set the title in the textView within the listView layout above
        tvExerciseName = (TextView) setsView.findViewById(R.id.tv_exercise_name);
        tvExerciseName.setText(exercise.getName());

        // Set the recyclerView list to be horizontal and pass in the exercise sets through the
        // adapter
        setList = (RecyclerView) setsView.findViewById(R.id.rv_exercise_sets);
        setList.setHasFixedSize(true);
        setList.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false));

        setList.setAdapter(new SetsAdapter(exercise.getSetsList()));
    }
}
