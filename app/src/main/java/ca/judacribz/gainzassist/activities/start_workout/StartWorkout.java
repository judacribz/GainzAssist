package ca.judacribz.gainzassist.activities.start_workout;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import butterknife.*;

import ca.judacribz.gainzassist.R;
import static ca.judacribz.gainzassist.activities.workouts_list.WorkoutsList.EXTRA_WORKOUT_NAME;
import static ca.judacribz.gainzassist.util.UI.*;

public class StartWorkout extends AppCompatActivity {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @BindView(R.id.tlay_navbar) TabLayout tabLayout;
    @BindView(R.id.vp_fmt_container) ViewPager viewPager;
    // --------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, R.layout.activity_start_workout, getIntent().getStringExtra(EXTRA_WORKOUT_NAME), true);

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return super.onSupportNavigateUp();
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////
}
