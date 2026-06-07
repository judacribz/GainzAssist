package ca.gainzassist.presentation.start_workout

sealed interface StartWorkoutRestoreDecision {
    data object StartFresh : StartWorkoutRestoreDecision
    data class RestoreFromJson(val sessionJson: String) : StartWorkoutRestoreDecision
}
