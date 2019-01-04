package ca.judacribz.gainzassist.activities.start_workout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.*;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos;
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter;
import ca.judacribz.gainzassist.models.*;
import java.util.ArrayList;
import org.parceler.Parcels;

import static ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT;
import static ca.judacribz.gainzassist.util.Misc.readValue;
import static ca.judacribz.gainzassist.util.Preferences.*;
import static ca.judacribz.gainzassist.util.UI.*;
import static com.facebook.rebound.ui.Util.dpToPx;

public class StartWorkout extends AppCompatActivity implements CurrWorkout.DataListener {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_HOW_TO_VID =
            "ca.judacribz.gainzassist.activities.start_workout.EXTRA_HOW_TO_VID";
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    Workout workout;
    ArrayList<Exercise> exercises;

    LayoutInflater layInflater;
    View setsView;

    CurrWorkout currWorkout = CurrWorkout.getInstance();

    TextView tvExerciseName;
    RecyclerView setList;

    RelativeLayout.LayoutParams lp;


    @BindView(R.id.tlay_navbar) TabLayout tabLayout;
    @BindView(R.id.vp_fmt_container) ViewPager viewPager;
    // --------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        workout = Parcels.unwrap(intent.getParcelableExtra(EXTRA_WORKOUT));
        exercises = workout.getExercises();
        setInitView(this, R.layout.activity_start_workout, workout.getName(), true);

        lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        int dpval = dpToPx(5, getResources());
        lp.setMargins(dpval, dpval, dpval, dpval);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (viewPager.getAdapter() == null) {
            setCurrSession();
        }
    }

    public void setCurrSession() {
        currWorkout.setDataListener(this);
        if (removeIncompleteWorkoutPref(this, workout.getName())) {
            currWorkout.setRetrievedWorkout(
                    readValue(getIncompleteSessionPref(this, workout.getName())),
                    workout
            );

            removeIncompleteSessionPref(this, workout.getName());
        } else {
            currWorkout.setCurrWorkout(workout);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        currWorkout.resetIndices();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        handleLeavingScreen();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        currWorkout.unsetTimer();
        handleLeavingScreen();
    }

    public void handleLeavingScreen() {
        String jsonStr = currWorkout.saveSessionState();
        if (!jsonStr.isEmpty()) {
            addIncompleteSessionPref(
                    this,
                    workout.getName(),
                    jsonStr
            );
        }
        addIncompleteWorkoutPref(this, workout.getName());

        currWorkout.resetLocks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mainMenu) {
        getMenuInflater().inflate(R.menu.menu_start_workout, mainMenu);

        return super.onCreateOptionsMenu(mainMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.act_how_to:
                Intent intent = new Intent(this, HowToVideos.class);
                intent.putExtra(EXTRA_HOW_TO_VID, currWorkout.getCurrExName());
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////


    // CurrWorkout.DataListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void warmupsGenerated(ArrayList<Exercise> warmups) {
        setupPager(warmups);
    }

    /* Setup fragments with page with icons for the tab bar */
    private void setupPager(ArrayList<Exercise> warmups) {
        layInflater = getLayoutInflater();

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

            Drawable icon;
            @Override
            public void onTabSelected(Tab tab) {
                super.onTabSelected(tab);
            }

            @Override
            public void onTabUnselected(Tab tab) {
                super.onTabUnselected(tab);
            }
        });

        if (warmups.size() == 0) {
            tabLayout.removeTabAt(0);
        }

        viewPager.setAdapter(new WorkoutPagerAdapter(
                getSupportFragmentManager(),
                exercises,
                warmups
        ));
        viewPager.setCurrentItem(tabLayout.getTabCount() - 2);
//        viewPager.setOffscreenPageLimit(3);
    }
    //CurrWorkout.DataListener//Override//////////////////////////////////////////////////////////


    /* Creates horizontal recycler view lists of  set#, reps, weights for each exercise and adds
     * dynamically to the view.
     * Called in ExercisesList and WarmupsList
     */
    @SuppressLint("InflateParams")
    public void displaySets(int id,
                            Exercise exercise,
                            LinearLayout llSets) {

        layInflater = getLayoutInflater();

        // Add the listView layout which containsExercise a textView and a recyclerVIew
        setsView = layInflater.inflate(R.layout.part_horizontal_rv, null);
        setsView.setLayoutParams(lp);
        setsView.setId(id);
        llSets.addView(setsView, 0);

        // ExerciseSet the title in the textView within the listView layout above
        tvExerciseName = (TextView) setsView.findViewById(R.id.tv_exercise_name);
        tvExerciseName.setText(exercise.getName());

        // ExerciseSet the recyclerView list to be horizontal and pass in the exercise sets through the
        // adapter
        setList = (RecyclerView) setsView.findViewById(R.id.rv_exercise_sets);
        setList.setHasFixedSize(true);
        setList.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        setList.setAdapter(new SetsAdapter(exercise.getSetsList()));
    }
}
