package ca.judacribz.gainzassist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.judacribz.gainzassist.activities.authentication.Login;
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos;
import ca.judacribz.gainzassist.activities.workouts_list.WorkoutsList;
import static ca.judacribz.gainzassist.util.UI.handleBackButton;
import static ca.judacribz.gainzassist.util.UI.setToolbar;

public class Main extends AppCompatActivity {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_LOGOUT_USER = "ca.judacribz.gainzassist.EXTRA_LOGOUT_USER";
    // --------------------------------------------------------------------------------------------


    @BindView(R.id.btn_resume_workout) Button btnResumeWorkout;
    @BindView(R.id.btn_workouts) Button btnWorkouts;
    @BindView(R.id.btn_step_counter) Button btnStepCounter;

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setToolbar(this, R.string.app_name, false);
    }

    @Override
    public void onBackPressed() {
        handleBackButton(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
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
