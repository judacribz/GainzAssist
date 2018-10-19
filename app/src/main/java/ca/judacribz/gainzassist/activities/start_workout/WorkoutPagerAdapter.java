package ca.judacribz.gainzassist.activities.start_workout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ca.judacribz.gainzassist.activities.start_workout.fragments.*;

public class WorkoutPagerAdapter extends FragmentPagerAdapter {

    // Constants
    // --------------------------------------------------------------------------------------------
    final private static Fragment[] FMTS = new Fragment[] {
            ListWarmups.newInstance(),
            WorkoutScreen.newInstance(),
            ListExercises.newInstance()
    };
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WorkoutPagerAdapter Constructor                                                           //
    // ######################################################################################### //
    WorkoutPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }
    // ######################################################################################### //

    // FragmentPagerAdapter Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Returns total number of pages */
    @Override
    public int getCount() {
        return FMTS.length;
    }

    /* Returns the fragment to display for that page */
    @Override
    public Fragment getItem(int position) {
        return FMTS[position];
    }
    //FragmentPagerAdapter//Override///////////////////////////////////////////////////////////////
}
