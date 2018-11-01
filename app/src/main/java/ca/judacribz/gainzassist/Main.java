package ca.judacribz.gainzassist;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.*;

import ca.judacribz.gainzassist.activities.authentication.Login;
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos;
import ca.judacribz.gainzassist.activities.workouts_list.WorkoutsList;
import ca.judacribz.gainzassist.async.FirebaseService;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;

import static ca.judacribz.gainzassist.util.Helper.getEmailFromPref;
import static ca.judacribz.gainzassist.util.UI.*;

public class Main extends AppCompatActivity {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_LOGOUT_USER = "ca.judacribz.gainzassist.EXTRA_LOGOUT_USER";
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------

    // UI Elements
    @BindView(R.id.btn_resume_workout) Button btnResumeWorkout;
    @BindView(R.id.btn_workouts) Button btnWorkouts;
    @BindView(R.id.btn_step_counter) Button btnStepCounter;
    // --------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private WorkoutViewModel workoutViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, R.layout.activity_main, R.string.app_name, false);

        btnResumeWorkout.setText(R.string.resume_workout);
        btnWorkouts.setText(R.string.workouts);
        btnStepCounter.setText(R.string.step_counter);

        SharedPreferences sharedPref = getSharedPreferences("user_info.file", Context.MODE_PRIVATE);
        Toast.makeText(this, "shared pref says " + getEmailFromPref(this), Toast.LENGTH_SHORT).show();

        workoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
//        workoutViewModel

//        workoutViewModel.deleteAllWorkouts();
//        workoutViewModel.insert(new Workout("yom", null));
//        workoutViewModel.insert(new Workout("bom", null));
//        workoutViewModel.insert(new Workout("sdf", null));
//        workoutViewModel.insert(new Workout("sddfds", null));

    }

    @Override
    public void onBackPressed() {
        handleBackButton(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mainMenu) {
        getMenuInflater().inflate(R.menu.menu_main, mainMenu);

        return super.onCreateOptionsMenu(mainMenu);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.act_settings:
                break;

            case R.id.act_logout:
                Intent logoutIntent = new Intent(this, Login.class);
                logoutIntent.putExtra(EXTRA_LOGOUT_USER, true);
                startActivity(logoutIntent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_workouts)
    public void startWorkoutsList() {
        startActivity(new Intent(this, WorkoutsList.class));
    }
    //=Click=Handling==============================================================================
}
