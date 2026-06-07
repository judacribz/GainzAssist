package ca.gainzassist.presentation.start_workout.workout

import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS

data class WorkoutProgressUiItem(
    val number: Int,
    val status: PROGRESS_STATUS
)
