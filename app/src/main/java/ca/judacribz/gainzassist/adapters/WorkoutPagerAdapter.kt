package ca.judacribz.gainzassist.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import ca.judacribz.gainzassist.activities.add_workout.ExEntry
import ca.judacribz.gainzassist.activities.start_workout.fragments.ExercisesList
import ca.judacribz.gainzassist.activities.start_workout.fragments.WarmupsList
import ca.judacribz.gainzassist.activities.start_workout.fragments.WorkoutScreen
import ca.judacribz.gainzassist.models.Exercise
import org.parceler.Parcels
import java.util.*

class WorkoutPagerAdapter : FragmentPagerAdapter {

    private val FMTS = ArrayList<Fragment>(
        listOf(
            WarmupsList.getInstance(),
            WorkoutScreen.getInstance(),
            ExercisesList.getInstance()
        )
    )

    private val FMTS_NO_WARMUPS = ArrayList<Fragment>(
        listOf(
            WorkoutScreen.getInstance(),
            ExercisesList.getInstance()
        )
    )

    private var fmts = FMTS
    private var numExs = 0
    private var baseId: Long = 0

    constructor(fragmentManager: FragmentManager, vararg exercises: ArrayList<Exercise>) : super(fragmentManager) {
        val bundle = Bundle()
        bundle.putParcelable(EXTRA_MAIN_EXERCISES, Parcels.wrap(exercises[0]))
        val warmups = exercises[1]
        if (warmups.size > 0) {
            bundle.putParcelable(EXTRA_WARMUPS, Parcels.wrap(exercises[1]))
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

    override fun getCount(): Int {
        return fmts.size
    }

    override fun getItem(position: Int): Fragment {
        return fmts[position]
    }

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
        val fmt = ExEntry()
        fmt.setInd(index)
        fmts.add(fmt)
    }

    fun removeFragment(index: Int, exercises: ArrayList<Exercise>) {
        fmts.removeAt(index)
        numExs--
        notifyChangeInPosition()
        var i: Int
        var exercise: Exercise
        val newFmts = ArrayList<Fragment>()
        for (fmt in fmts) {
            i = fmts.indexOf(fmt)
            val newFmt = ExEntry()
            newFmt.setInd(i)
            exercise = exercises[i]
            if (exercise.name != null) {
                newFmt.updateExFields(exercise)
            }
            newFmts.add(newFmt)
        }
        fmts.clear()
        fmts.addAll(newFmts)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getItemId(position: Int): Long {
        return baseId + position
    }

    private fun notifyChangeInPosition() {
        baseId += (count + 1).toLong()
    }

    fun hideDelete() {
        (fmts[0] as ExEntry).hideDelete()
    }

    companion object {
        const val EXTRA_WARMUPS = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_WARMUPS"
        const val EXTRA_MAIN_EXERCISES = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_MAIN_EXERCISES"
        const val EXTRA_EX_INDEX = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_EX_INDEX"
    }
}
