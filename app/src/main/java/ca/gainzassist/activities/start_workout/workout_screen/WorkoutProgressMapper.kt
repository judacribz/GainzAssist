package ca.gainzassist.activities.start_workout.workout_screen

import android.util.SparseArray
import ca.gainzassist.ui.adapters.SingleItemAdapter.PROGRESS_STATUS
import ca.gainzassist.ui.adapters.SingleItemAdapter.PROGRESS_STATUS.FAIL
import ca.gainzassist.ui.adapters.SingleItemAdapter.PROGRESS_STATUS.FAIL_SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.PROGRESS_STATUS.SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.PROGRESS_STATUS.SUCCESS
import ca.gainzassist.ui.adapters.SingleItemAdapter.PROGRESS_STATUS.SUCCESS_SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.PROGRESS_STATUS.UNSELECTED

object WorkoutProgressMapper {

    fun setupProgress(
        numItems: Int,
        selectedOneBasedIndex: Int
    ): SparseArray<PROGRESS_STATUS> {
        val progressStatus = SparseArray<PROGRESS_STATUS>()
        for (i in 0 until numItems) {
            progressStatus.put(i, UNSELECTED)
        }
        progressStatus.put(selectedOneBasedIndex - 1, SELECTED)
        return progressStatus
    }

    fun toProgressUiItems(
        progress: SparseArray<PROGRESS_STATUS>?,
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
        progress: SparseArray<PROGRESS_STATUS>,
        selectedOneBasedIndex: Int
    ) {
        deselectCurrent(progress)
        val zeroBased = selectedOneBasedIndex - 1
        val status = progress.get(zeroBased)
        when (status) {
            SUCCESS -> progress.put(zeroBased, SUCCESS_SELECTED)
            FAIL -> progress.put(zeroBased, FAIL_SELECTED)
            else -> progress.put(zeroBased, SELECTED)
        }
    }

    fun setCurrentOneBased(
        progress: SparseArray<PROGRESS_STATUS>,
        selectedOneBasedIndex: Int,
        success: Boolean
    ) {
        selectOneBased(progress, selectedOneBasedIndex)
        if (selectedOneBasedIndex > 1) {
            progress.put(selectedOneBasedIndex - 2, if (success) SUCCESS else FAIL)
        }
    }

    private fun deselectCurrent(progress: SparseArray<PROGRESS_STATUS>) {
        for (i in 0 until progress.size()) {
            val key = progress.keyAt(i)
            when (progress.get(key)) {
                SELECTED -> progress.put(key, UNSELECTED)
                SUCCESS_SELECTED -> progress.put(key, SUCCESS)
                FAIL_SELECTED -> progress.put(key, FAIL)
                else -> Unit
            }
        }
    }
}
