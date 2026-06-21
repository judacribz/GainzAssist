package ca.gainzassist.feature.start_workout.presentation.state

import ca.gainzassist.ui.ProgressStatus

data class WorkoutScreenState(
    val exerciseTitle: String = "",
    val setNumText: String = "",
    val repsText: String = "",
    val weightText: String = "",
    val timerText: String = "",
    val currentWeight: Float = 0f,
    val currentEquipment: String = "",
    val isMinReps: Boolean = false,
    val isMinWeight: Boolean = false,
    val isFinishSetVisible: Boolean = true,
    val isUpdateSetVisible: Boolean = false,
    val isResumeWorkoutVisible: Boolean = false,
    val exerciseProgress: List<WorkoutProgressUiItem> = emptyList(),
    val setProgress: List<WorkoutProgressUiItem> = emptyList()
)

data class WorkoutProgressUiItem(
    val number: Int,
    val status: ProgressStatus
)