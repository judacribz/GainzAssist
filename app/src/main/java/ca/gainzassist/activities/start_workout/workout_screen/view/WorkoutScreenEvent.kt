package ca.gainzassist.activities.start_workout.workout_screen.view

sealed interface WorkoutScreenEvent {
    object FinishWorkout : WorkoutScreenEvent
    object SaveProgress : WorkoutScreenEvent
    data class Error(val message: String) : WorkoutScreenEvent
}
