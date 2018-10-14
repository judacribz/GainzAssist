package ca.judacribz.gainzassist.activities.start_workout;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import ca.judacribz.gainzassist.activities.start_workout.fragments.*;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Workout;

public class WorkoutPagerAdapter extends FragmentPagerAdapter {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static String EXTRA_WARMUPS = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_WARMUPS";
    public static String EXTRA_WORKOUT = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_WORKOUT";
    final private static Fragment[] FMTS = new Fragment[] {
            CurrWarmups.newInstance(),
            CurrWorkout.newInstance(),
            CurrExercises.newInstance()
    };
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    Bundle bundle = new Bundle();
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WorkoutPagerAdapter Constructor                                                           //
    // ######################################################################################### //
    WorkoutPagerAdapter(FragmentManager fragmentManager, Workout workout, ArrayList<Exercise> warmups) {
        super(fragmentManager);

        bundle.putParcelable(EXTRA_WORKOUT, workout);
        bundle.putParcelableArrayList(EXTRA_WARMUPS, warmups);

        for (Fragment fmt : FMTS) {
            fmt.setArguments(bundle);
        }

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
