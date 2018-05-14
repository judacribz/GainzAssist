package ca.judacribz.gainzassist.activity_start_workout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ca.judacribz.gainzassist.activity_start_workout.fragments.*;

public class WorkoutPagerAdapter extends FragmentPagerAdapter {

    // Constants
    // --------------------------------------------------------------------------------------------
    final private static int NUM_ITEMS = 3;
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
        return NUM_ITEMS;
    }

    /* Returns the fragment to display for that page */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return WarmupExercises.newInstance();
            case 1:
                return MainWorkout.newInstance();
            case 2:
                return MainExercises.newInstance();
            default:
                return null;
        }
    }
    //FragmentPagerAdapter//Override///////////////////////////////////////////////////////////////
}
