package ca.gainzassist.presentation.start_workout.workout

data class WorkoutScreenState(
    val exerciseTitle: String = "",
    val setNumText: String = "",
    val repsText: String = "",
    val weightText: String = "",
    val timerText: String = "",
    val currentWeight: String = "",
    val currentEquipment: String = "",
    val isMinReps: Boolean = false,
    val isMinWeight: Boolean = false,
    val isFinishSetVisible: Boolean = false,
    val isUpdateSetVisible: Boolean = false,
    val isResumeWorkoutVisible: Boolean = false,
    val exerciseProgress: Int = 0,
    val setProgress: Int = 0
)
