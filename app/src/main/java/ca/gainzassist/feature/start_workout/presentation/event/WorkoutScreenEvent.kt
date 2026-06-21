package ca.gainzassist.feature.start_workout.presentation.event

sealed interface WorkoutScreenEvent {
    object FinishWorkout : WorkoutScreenEvent
    object SaveProgress : WorkoutScreenEvent
    data class Error(val message: String) : WorkoutScreenEvent
}
