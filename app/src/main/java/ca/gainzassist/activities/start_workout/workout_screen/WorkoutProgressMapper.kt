package ca.gainzassist.activities.start_workout.workout_screen

import android.util.SparseArray
import ca.gainzassist.activities.start_workout.workout_screen.view.WorkoutProgressUiItem
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.FAIL
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.FAIL_SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SUCCESS
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SUCCESS_SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.UNSELECTED
import androidx.core.util.size

object WorkoutProgressMapper {

    fun setupProgress(
        numItems: Int,
        selectedOneBasedIndex: Int
    ): SparseArray<ProgressStatus> {
        val progressStatus = SparseArray<ProgressStatus>()
        for (i in 0 until numItems) {
            progressStatus.put(i, UNSELECTED)
        }
        progressStatus.put(selectedOneBasedIndex - 1, SELECTED)
        return progressStatus
    }

    fun toProgressUiItems(
        progress: SparseArray<ProgressStatus>?,
        count: Int
    ): List<WorkoutProgressUiItem> {
        if (progress == null) return emptyList()
        val items = mutableListOf<WorkoutProgressUiItem>()
        for (zeroBased in 0 until count) {
            items.add(
                WorkoutProgressUiItem(
                    number = zeroBased + 1,
                    status = progress[zeroBased] ?: UNSELECTED
                )
            )
        }
        return items
    }

    fun selectOneBased(
        progress: SparseArray<ProgressStatus>,
        selectedOneBasedIndex: Int
    ) {
        deselectCurrent(progress)
        val zeroBased = selectedOneBasedIndex - 1
        val status = progress[zeroBased]
        when (status) {
            SUCCESS -> progress.put(zeroBased, SUCCESS_SELECTED)
            FAIL -> progress.put(zeroBased, FAIL_SELECTED)
            else -> progress.put(zeroBased, SELECTED)
        }
    }

    fun setCurrentOneBased(
        progress: SparseArray<ProgressStatus>,
        selectedOneBasedIndex: Int,
        success: Boolean
    ) {
        selectOneBased(progress, selectedOneBasedIndex)
        if (selectedOneBasedIndex > 1) {
            progress.put(selectedOneBasedIndex - 2, if (success) SUCCESS else FAIL)
        }
    }

    private fun deselectCurrent(progress: SparseArray<ProgressStatus>) {
        for (i in 0 until progress.size) {
            val key = progress.keyAt(i)
            when (progress[key]) {
                SELECTED -> progress.put(key, UNSELECTED)
                SUCCESS_SELECTED -> progress.put(key, SUCCESS)
                FAIL_SELECTED -> progress.put(key, FAIL)
                else -> Unit
            }
        }
    }
}
