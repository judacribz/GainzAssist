package ca.judacribz.gainzassist.activities.start_workout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import ca.judacribz.gainzassist.activities.start_workout.fragments.*;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Workout;
import org.parceler.Parcels;

public class WorkoutPagerAdapter extends FragmentPagerAdapter {

    // Constants
    // --------------------------------------------------------------------------------------------
    final public static String EXTRA_WARMUPS = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_WARMUPS";
    final public static String EXTRA_WORKOUT = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_WORKOUT";
    final private static Fragment[] FMTS = new Fragment[] {
            WarmupsList.newInstance(),
            WorkoutScreen.newInstance(),
            ExercisesList.newInstance()
    };
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private Bundle bundle = new Bundle();
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WorkoutPagerAdapter Constructor                                                           //
    // ######################################################################################### //
    WorkoutPagerAdapter(FragmentManager fragmentManager, Workout workout, ArrayList<Exercise> warmups) {
        super(fragmentManager);

        bundle.putParcelable(EXTRA_WORKOUT, Parcels.wrap(workout));
        bundle.putParcelable(EXTRA_WARMUPS, Parcels.wrap(new Workout("warmups", warmups)));

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