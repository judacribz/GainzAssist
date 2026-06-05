package ca.gainzassist.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ca.gainzassist.activities.add_workout.ExEntry
import ca.gainzassist.models.Exercise

class WorkoutPagerAdapter : FragmentPagerAdapter {

    private var fmts: ArrayList<Fragment> = ArrayList()
    private var numExs = 0
    private var baseId = 0

    constructor(fragmentManager: FragmentManager, numExs: Int) : super(fragmentManager) {
        this.numExs = numExs
        fmts = ArrayList()
        newEntries()
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
        for (fmt in fmts) {
            i = fmts.indexOf(fmt)
            val newFmt = ExEntry()
            newFmt.setInd(i)
            exercise = exercises[i]
            if (exercise.name != null) {
                newFmt.updateExFields(exercise)
            }
            fmts[i] = newFmt
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getItemId(position: Int): Long {
        return (baseId + position).toLong()
    }

    private fun notifyChangeInPosition() {
        baseId += count + 1
    }

    fun hideDelete() {
        (fmts[0] as ExEntry).hideDelete()
    }
}
