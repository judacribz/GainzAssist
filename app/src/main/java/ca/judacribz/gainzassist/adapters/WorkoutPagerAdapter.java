package ca.judacribz.gainzassist.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity.*;
import android.support.v4.app.*;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import ca.judacribz.gainzassist.activities.add_workout.ExEntry;
import ca.judacribz.gainzassist.activities.start_workout.fragments.*;
import ca.judacribz.gainzassist.models.Exercise;
import com.orhanobut.logger.Logger;
import org.parceler.Parcels;

import static ca.judacribz.gainzassist.util.Misc.shrinkTo;

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

    public WorkoutPagerAdapter(FragmentManager fragmentManager, int numExs, @Nullable List<Exercise> exercises) {
        super(fragmentManager);
        this.numExs = numExs;
        fmts = new ArrayList<>();

        newEntries(exercises);
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


    public void addTab() {
        this.numExs++;
        newEntry(getCount(), null);
    }

    public void newEntries(@Nullable List<Exercise> exercises) {
        Exercise ex;
        for (int i = 0; i < this.numExs; i++) {
            ex = (exercises != null) ? exercises.get(i) : null;
            newEntry(i, ex);
        }
    }

    public void newEntry(int index, @Nullable Exercise exercise) {
        Fragment fmt = new ExEntry();
        ((ExEntry) fmt).setInd(index);
        if (exercise != null) {
            ((ExEntry) fmt).updateExFields(exercise);
        }
        fmts.add(fmt);
    }

    public void removeFragment(int index, ArrayList<Exercise> exercises) {
        this.fmts.remove(index);

        notifyChangeInPosition(1);
        int i;
        Exercise exercise;
        for (Fragment fmt : fmts) {
            i = fmts.indexOf(fmt);

            Logger.d("INDEX FM INIT " +((ExEntry) fmt).getInd() );
            fmt = new ExEntry();
            ((ExEntry) fmt).setInd(i);

            Logger.d("INDEX FM END " +((ExEntry) fmt).getInd() );
            exercise = exercises.get(i);
            if (exercise != null) {
                ((ExEntry) fmt).updateExFields(exercise);
            }

            fmts.set(i, fmt);
        }
    }


    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return WorkoutPagerAdapter.POSITION_NONE;
    }

int baseId = 0;
    @Override
    public long getItemId(int position) {
        // give an ID different from position when position has been changed
        return baseId + position;
    }

    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment
     * @param n number of items which have been changed
     */
    public void notifyChangeInPosition(int n) {
        // shift the ID returned by getItemId outside the range of all previous fragments
        baseId += getCount() + n;
    }
}