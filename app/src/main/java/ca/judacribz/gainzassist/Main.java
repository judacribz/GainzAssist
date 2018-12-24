package ca.judacribz.gainzassist;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;

import android.view.MenuItem;
import butterknife.*;

import ca.judacribz.gainzassist.activities.authentication.Login;
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.util.UI.*;

public class Main extends AppCompatActivity {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_LOGOUT_USER = "ca.judacribz.gainzassist.EXTRA_LOGOUT_USER";
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    LayoutInflater layInflater;



    @BindView(R.id.tlay_navbar) TabLayout tabLayout;
    @BindView(R.id.vp_fmt_container) ViewPager viewPager;
    // --------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private WorkoutViewModel workoutViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, R.layout.activity_main, R.string.app_name, false);

        setupPager();
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

        ArrayList<Fragment> fmts = new ArrayList<>();
        fmts.add(Resume.getInstance());
        fmts.add(Home.getInstance());
        fmts.add(Settings.getInstance());
        viewPager.setAdapter(new WorkoutPagerAdapter(
                getSupportFragmentManager(),
                fmts
        ));
        viewPager.setCurrentItem(1);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.act_settings:
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

}
