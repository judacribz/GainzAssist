package ca.judacribz.gainzassist.activities.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;

import android.view.MenuItem;
import butterknife.*;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry;
import ca.judacribz.gainzassist.activities.authentication.Login;
import ca.judacribz.gainzassist.activities.main.fragments.Home;
import ca.judacribz.gainzassist.activities.main.fragments.Resume;
import ca.judacribz.gainzassist.activities.main.fragments.Settings;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter;
import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;
import ca.judacribz.gainzassist.util.UI;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;

import static ca.judacribz.gainzassist.util.UI.*;

public class Main extends AppCompatActivity implements
        MaterialSearchView.OnQueryTextListener,
        OnWorkoutReceivedListener {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_LOGOUT_USER = "ca.judacribz.gainzassist.EXTRA_LOGOUT_USER";
    public static final String EXTRA_WORKOUT
            = "ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT";
    public final Fragment[] fmts = new Fragment[] {
            Resume.getInstance(),
            Home.getInstance(),
            Settings.getInstance()
    };
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    LayoutInflater layInflater;

    @BindView(ca.judacribz.gainzassist.R.id.tlay_navbar) TabLayout tabLayout;
    @BindView(ca.judacribz.gainzassist.R.id.vp_fmt_container) ViewPager viewPager;
    @BindView(R.id.msvWorkouts) MaterialSearchView searchView;
    // --------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private WorkoutViewModel workoutViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UI.setInitView(this, ca.judacribz.gainzassist.R.layout.activity_main, ca.judacribz.gainzassist.R.string.app_name, false);

        setupPager();

        searchView.setOnQueryTextListener(this);
        searchView.setVoiceSearch(true);
    }

    private void setupPager() {
        layInflater = getLayoutInflater();

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
            }
        });

        viewPager.setAdapter(new WorkoutPagerAdapter(
                getSupportFragmentManager(),
                Arrays.asList(fmts)
        ));
        viewPager.setCurrentItem(1);
    }
    @Override
    public void onBackPressed() {
        handleBackButton(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mainMenu) {
        getMenuInflater().inflate(ca.judacribz.gainzassist.R.menu.menu_main, mainMenu);

        MenuItem item = mainMenu.findItem(R.id.act_search);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(mainMenu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // TextWatcher Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onQueryTextChange(String newText) {
        ((Home)fmts[1]).onQueryTextChange(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    //TextWatcher//Override////////////////////////////////////////////////////////////////////////


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.act_add_workout:
                startActivity(new Intent(this, WorkoutEntry.class));
                break;

            case R.id.act_logout:
                Intent logoutIntent = new Intent(this, Login.class);
                logoutIntent.putExtra(EXTRA_LOGOUT_USER, true);
                startActivity(logoutIntent);

                ViewModelProviders.of(this).get(WorkoutViewModel.class).deleteAllWorkouts();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWorkoutsReceived(Workout workout) {
        ((Home)fmts[1]).intent.putExtra(((Home)fmts[1]).extraKey, Parcels.wrap(workout));
        startActivity(((Home)fmts[1]).intent);
    }
}
