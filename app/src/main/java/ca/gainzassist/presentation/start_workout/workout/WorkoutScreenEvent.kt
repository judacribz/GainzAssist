package ca.gainzassist.presentation.start_workout.workout

sealed interface WorkoutScreenEvent {
    object FinishWorkout : WorkoutScreenEvent
    object SaveProgress : WorkoutScreenEvent
    data class Error(val message: String) : WorkoutScreenEvent
}
