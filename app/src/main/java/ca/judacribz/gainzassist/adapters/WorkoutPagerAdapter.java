package ca.judacribz.gainzassist.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity.*;
import android.support.v4.app.*;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
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


    final private ArrayList<Fragment> FMTS = new ArrayList<Fragment>()  {
        {
            add(WarmupsList.getInstance());
            add(WorkoutScreen.getInstance());
            add(ExercisesList.getInstance());
        }
    };

    final private ArrayList<Fragment> FMTS_NO_WARMUPS = new ArrayList<Fragment>()  {
        {
            add(WorkoutScreen.getInstance());
            add(ExercisesList.getInstance());
        }
    };

    private FragmentManager fragmentManager;
    private int numExs;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private Bundle bundle = new Bundle();
    // --------------------------------------------------------------------------------------------
    ArrayList<Fragment> fmts = FMTS;
    // ######################################################################################### //
    // WorkoutPagerAdapter Constructor                                                           //
    // ######################################################################################### //
    @SafeVarargs
    public WorkoutPagerAdapter(FragmentManager fragmentManager, ArrayList<Exercise>... exercises) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;

        bundle.putParcelable(EXTRA_MAIN_EXERCISES, Parcels.wrap(exercises[0]));
        ArrayList<Exercise> warmups = exercises[1];

        if (warmups.size() > 0) {
            bundle.putParcelable(EXTRA_WARMUPS, Parcels.wrap(exercises[1]));
        } else {
            fmts =  FMTS_NO_WARMUPS;
        }
        for (Fragment fmt : fmts) {
            fmt.setArguments(bundle);
        }
    }

    public WorkoutPagerAdapter(FragmentManager fragmentManager, int numExs) {
        super(fragmentManager);
        this.numExs = numExs;
        fmts = new ArrayList<>();

        for (int i = 0; i < this.numExs; i++) {
            newEntry(i);
        }
    }

    public WorkoutPagerAdapter(FragmentManager fragmentManager, List<Fragment> fmts) {
        super(fragmentManager);
        this.fmts = (ArrayList<Fragment>) fmts;
    }
    // ######################################################################################### //

    // FragmentPagerAdapter Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Returns total number of pages */
    @Override
    public int getCount() {
        return fmts.size();
    }

    /* Returns the fragment to display for that page */
    @Override
    public Fragment getItem(int position) {
        return fmts.get(position);
    }
    //FragmentPagerAdapter//Override///////////////////////////////////////////////////////////////


    public void notifyDataSetChanged(SparseArray<Exercise> exercises) {
        notifyDataSetChanged();

        ExEntry exEntryFmt;
        int i;
        for (Fragment fmt : fmts) {
            exEntryFmt = (ExEntry) fmt;
            i = fmts.indexOf(fmt);

            exEntryFmt.setInd(i);
            exEntryFmt.updateExFields(exercises.get(i));
        }
    }

    public void addTab() {
        this.numExs++;
        newEntry(getCount());
    }

    public void newEntry(int index) {
        Fragment fmt = new ExEntry();
        ((ExEntry) fmt).setInd(index);
        fmts.add(fmt);
    }

    public void removeTabFragment(int index, FragmentManager fragmentManager) {
        this.numExs--;

        fmts.remove(index);
        fmts.trimToSize();
    }
}