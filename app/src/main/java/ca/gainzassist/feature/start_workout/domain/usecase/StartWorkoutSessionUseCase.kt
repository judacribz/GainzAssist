package ca.gainzassist.feature.start_workout.domain.usecase

import ca.gainzassist.feature.start_workout.domain.model.StartWorkoutRestoreDecision
import ca.gainzassist.domain.usecase.session.GetIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteWorkoutUseCase

class StartWorkoutSessionUseCase(
    private val removeIncompleteWorkoutUseCase: RemoveIncompleteWorkoutUseCase,
    private val getIncompleteSessionUseCase: GetIncompleteSessionUseCase,
    private val removeIncompleteSessionUseCase: RemoveIncompleteSessionUseCase
) {
    suspend operator fun invoke(workoutName: String): StartWorkoutRestoreDecision {
        if (workoutName.isBlank()) {
            return StartWorkoutRestoreDecision.StartFresh
        }

        val hasIncompleteWorkout = removeIncompleteWorkoutUseCase(workoutName)
        if (!hasIncompleteWorkout) {
            return StartWorkoutRestoreDecision.StartFresh
        }

        val sessionJson = getIncompleteSessionUseCase(workoutName)
        if (sessionJson.isNullOrEmpty()) {
            removeIncompleteSessionUseCase(workoutName)
            return StartWorkoutRestoreDecision.StartFresh
        }

        removeIncompleteSessionUseCase(workoutName)
        return StartWorkoutRestoreDecision.RestoreFromJson(sessionJson)
    }
}
