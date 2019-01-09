package ca.judacribz.gainzassist.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;

import android.view.View;
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

    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private ArrayList<Fragment> fmts = FMTS;
    private int
            numExs,
            baseId = 0;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WorkoutPagerAdapter Constructor                                                           //
    // ######################################################################################### //
    @SafeVarargs
    public WorkoutPagerAdapter(FragmentManager fragmentManager, ArrayList<Exercise>... exercises) {
        super(fragmentManager);

        Bundle bundle = new Bundle();
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

        newEntries();
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
        newEntry(getCount());
    }

    public void newEntries() {
        for (int i = 0; i < this.numExs; i++) {
            newEntry(i);
        }
    }

    public void newEntry(int index) {
        Fragment fmt = new ExEntry();
        ((ExEntry) fmt).setInd(index);
        fmts.add(fmt);
    }

    public void removeFragment(int index, ArrayList<Exercise> exercises) {
        this.fmts.remove(index);
        this.numExs--;

        notifyChangeInPosition();
        int i;
        Exercise exercise;
        for (Fragment fmt : this.fmts) {
            i = fmts.indexOf(fmt);
            fmt = new ExEntry();

            ((ExEntry) fmt).setInd(i);

            exercise = exercises.get(i);
            if (exercise.getName() != null) {
                ((ExEntry) fmt).updateExFields(exercise);
            }

            fmts.set(i, fmt);
        }
    }


    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(@NonNull Object object) {
        // refresh all fragments when data set changed
        return WorkoutPagerAdapter.POSITION_NONE;
    }


    @Override
    public long getItemId(int position) {
        // give an ID different from position when position has been changed
        return baseId + position;
    }

    private void notifyChangeInPosition() {
        baseId += getCount() + 1;
    }

    public void hideDelete() {
        ((ExEntry) this.fmts.get(0)).hideDelete();
    }
}