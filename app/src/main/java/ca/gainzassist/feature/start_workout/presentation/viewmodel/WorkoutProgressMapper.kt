package ca.gainzassist.feature.start_workout.presentation.viewmodel

import ca.gainzassist.feature.start_workout.presentation.state.WorkoutProgressUiItem
import ca.gainzassist.ui.ProgressStatus
import ca.gainzassist.ui.ProgressStatus.FAIL
import ca.gainzassist.ui.ProgressStatus.FAIL_SELECTED
import ca.gainzassist.ui.ProgressStatus.SELECTED
import ca.gainzassist.ui.ProgressStatus.SUCCESS
import ca.gainzassist.ui.ProgressStatus.SUCCESS_SELECTED
import ca.gainzassist.ui.ProgressStatus.UNSELECTED

object WorkoutProgressMapper {

    fun setupProgress(
        numItems: Int,
        selectedOneBasedIndex: Int
    ): MutableMap<Int, ProgressStatus> {
        val progressStatus = mutableMapOf<Int, ProgressStatus>()
        for (i in 0 until numItems) {
            progressStatus[i] = UNSELECTED
        }
        progressStatus[selectedOneBasedIndex - 1] = SELECTED
        return progressStatus
    }

    fun toProgressUiItems(
        progress: Map<Int, ProgressStatus>?,
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
        progress: MutableMap<Int, ProgressStatus>,
        selectedOneBasedIndex: Int
    ) {
        deselectCurrent(progress)
        val zeroBased = selectedOneBasedIndex - 1
        val status = progress[zeroBased]
        when (status) {
            SUCCESS -> progress[zeroBased] = SUCCESS_SELECTED
            FAIL -> progress[zeroBased] = FAIL_SELECTED
            else -> progress[zeroBased] = SELECTED
        }
    }

    fun setCurrentOneBased(
        progress: MutableMap<Int, ProgressStatus>,
        selectedOneBasedIndex: Int,
        success: Boolean
    ) {
        selectOneBased(progress, selectedOneBasedIndex)
        if (selectedOneBasedIndex > 1) {
            progress[selectedOneBasedIndex - 2] = if (success) SUCCESS else FAIL
        }
    }

    private fun deselectCurrent(progress: MutableMap<Int, ProgressStatus>) {
        for ((key, value) in progress) {
            when (value) {
                SELECTED -> progress[key] = UNSELECTED
                SUCCESS_SELECTED -> progress[key] = SUCCESS
                FAIL_SELECTED -> progress[key] = FAIL
                else -> Unit
            }
        }
    }
}
