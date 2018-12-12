package ca.judacribz.gainzassist.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import ca.judacribz.gainzassist.activities.add_workout.ExEntry;
import ca.judacribz.gainzassist.activities.start_workout.fragments.*;
import ca.judacribz.gainzassist.models.Exercise;
import org.parceler.Parcels;

public class WorkoutPagerAdapter extends FragmentPagerAdapter {

    // Constants
    // --------------------------------------------------------------------------------------------
    final public static String EXTRA_WARMUPS =
            "ca.judacribz.gainzassist.activities.start_workout.EXTRA_WARMUPS";
    final public static String EXTRA_MAIN_EXERCISES =
            "ca.judacribz.gainzassist.activities.start_workout.EXTRA_MAIN_EXERCISES";
    final public static String EXTRA_EX_INDEX =
            "ca.judacribz.gainzassist.activities.start_workout.EXTRA_EX_INDEX";

    final private static Fragment[] FMTS = new Fragment[] {
            WarmupsList.getInstance(),
            WorkoutScreen.getInstance(),
            ExercisesList.getInstance()
    };
    final private static Fragment[] FMTS_NO_WARMUPS = new Fragment[] {
            WorkoutScreen.getInstance(),
            ExercisesList.getInstance()
    };
    private int numExs;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private Bundle bundle = new Bundle();
    // --------------------------------------------------------------------------------------------
    Fragment[] fmts = FMTS;
    // ######################################################################################### //
    // WorkoutPagerAdapter Constructor                                                           //
    // ######################################################################################### //
    @SafeVarargs
    public WorkoutPagerAdapter(FragmentManager fragmentManager, ArrayList<Exercise>... exercises) {
        super(fragmentManager);

//        Log.d("WARMUPS", "reps" + warmups.get(0).getSetsList().size());
        bundle.putParcelable(EXTRA_MAIN_EXERCISES, Parcels.wrap(exercises[0]));
        ArrayList<Exercise> warmups = exercises[1];

        if (warmups.size() > 0) {
            bundle.putParcelable(EXTRA_WARMUPS, Parcels.wrap(exercises[1]));
        } else {
            fmts = FMTS_NO_WARMUPS;
        }
        for (Fragment fmt : fmts) {
            fmt.setArguments(bundle);
        }
    }

    public WorkoutPagerAdapter(FragmentManager fragmentManager, int numExs) {
        super(fragmentManager);
        this.numExs = numExs;
        fmts = new Fragment[numExs];

        for (int i = 0; i < fmts.length; i++) {
            fmts[i] = new ExEntry();
            ((ExEntry) fmts[i]).setInd(i);
        }
    }
    public void addTab() {
        this.numExs++;
        Fragment[] tempFmts = new Fragment[this.numExs];

        System.arraycopy(fmts, 0, tempFmts, 0, fmts.length);
        tempFmts[fmts.length] = new ExEntry();
        ((ExEntry) tempFmts[fmts.length]).setInd(fmts.length);
        fmts = new Fragment[this.numExs];

        System.arraycopy(tempFmts, 0, fmts, 0, fmts.length);
        fmts = tempFmts;
    }
    // ######################################################################################### //

    // FragmentPagerAdapter Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Returns total number of pages */
    @Override
    public int getCount() {
        return fmts.length;
    }

    /* Returns the fragment to display for that page */
    @Override
    public Fragment getItem(int position) {
        return fmts[position];
    }
    //FragmentPagerAdapter//Override///////////////////////////////////////////////////////////////

}