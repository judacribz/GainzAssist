package ca.gainzassist.feature.start_workout.domain.model

sealed interface StartWorkoutRestoreDecision {
    data object StartFresh : StartWorkoutRestoreDecision
    data class RestoreFromJson(val sessionJson: String) : StartWorkoutRestoreDecision
}
