package ca.judacribz.gainzassist.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ca.judacribz.gainzassist.activities.add_workout.ExEntryFragment
import ca.judacribz.gainzassist.activities.start_workout.fragments.ExercisesList
import ca.judacribz.gainzassist.activities.start_workout.fragments.WarmupsList
import ca.judacribz.gainzassist.activities.start_workout.fragments.WorkoutScreen
import ca.judacribz.gainzassist.models.Exercise

class WorkoutPagerAdapter : FragmentPagerAdapter {
    private val FMTS: ArrayList<Fragment> = object : ArrayList<Fragment>() {
        init {
            add(WarmupsList.getInstance())
            add(WorkoutScreen.instance)
            add(ExercisesList.instance)
        }
    }
    private val FMTS_NO_WARMUPS: ArrayList<Fragment> = object : ArrayList<Fragment>() {
        init {
            add(WorkoutScreen.instance)
            add(ExercisesList.instance)
        }
    }

    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    private var fmts = FMTS
    private var numExs = 0
    private var baseId = 0

    // --------------------------------------------------------------------------------------------
    // ######################################################################################### //
    // WorkoutPagerAdapter Constructor                                                           //
    // ######################################################################################### //
    @SafeVarargs
    constructor(fragmentManager: FragmentManager, vararg exercises: ArrayList<Exercise>) : super(fragmentManager) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(EXTRA_MAIN_EXERCISES,exercises[0])
        val warmups = exercises[1]
        if (warmups.size > 0) {
            bundle.putParcelableArrayList(EXTRA_WARMUPS, exercises[1])
        } else {
            fmts = FMTS_NO_WARMUPS
        }
        for (fmt in fmts) {
            fmt.arguments = bundle
        }
    }

    constructor(fragmentManager: FragmentManager, numExs: Int) : super(fragmentManager) {
        this.numExs = numExs
        fmts = ArrayList()
        newEntries()
    }

    constructor(fragmentManager: FragmentManager, fmts: List<Fragment>) : super(fragmentManager) {
        this.fmts = fmts as ArrayList<Fragment>
    }

    // ######################################################################################### //
    // FragmentPagerAdapter Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Returns total number of pages */
    override fun getCount(): Int {
        return fmts.size
    }

    /* Returns the fragment to display for that page */
    override fun getItem(position: Int): Fragment {
        return fmts[position]
    }

    //FragmentPagerAdapter//Override///////////////////////////////////////////////////////////////
    fun addTab() {
        numExs++
        newEntry(count)
    }

    fun newEntries() {
        for (i in 0 until numExs) {
            newEntry(i)
        }
    }

    fun newEntry(index: Int) {
        val fmt: Fragment = ExEntryFragment()
        (fmt as ExEntryFragment).setInd(index)
        fmts.add(fmt)
    }

    fun removeFragment(index: Int, exercises: ArrayList<Exercise>) {
        fmts.removeAt(index)
        numExs--
        notifyChangeInPosition()
        var i: Int
        var exercise: Exercise
        for (fmt in fmts) {
            i = fmts.indexOf(fmt)
            val format = ExEntryFragment()
            format.setInd(i)
            exercise = exercises[i]
            if (exercise.name != null) {
                format.updateExFields(exercise)
            }
            fmts[i] = format
        }
    }

    //this is called when notifyDataSetChanged() is called
    override fun getItemPosition(`object`: Any): Int {
        // refresh all fragments when data set changed
        return POSITION_NONE
    }

    override fun getItemId(position: Int): Long {
        // give an ID different from position when position has been changed
        return (baseId + position).toLong()
    }

    private fun notifyChangeInPosition() {
        baseId += count + 1
    }

    fun hideDelete() {
        (fmts[0] as ExEntryFragment).hideDelete()
    }

    companion object {
        // Constants
        // --------------------------------------------------------------------------------------------
        const val EXTRA_WARMUPS = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_WARMUPS"
        const val EXTRA_MAIN_EXERCISES = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_MAIN_EXERCISES"
        const val EXTRA_EX_INDEX = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_EX_INDEX"
    }
}