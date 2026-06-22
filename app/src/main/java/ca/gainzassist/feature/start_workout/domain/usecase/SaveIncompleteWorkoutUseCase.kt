package ca.gainzassist.feature.start_workout.domain.usecase

import ca.gainzassist.domain.usecase.session.AddIncompleteWorkoutUseCase
import ca.gainzassist.domain.usecase.session.SaveIncompleteSessionUseCase

class SaveIncompleteWorkoutUseCase(
    private val saveIncompleteSessionUseCase: SaveIncompleteSessionUseCase,
    private val addIncompleteWorkoutUseCase: AddIncompleteWorkoutUseCase
) {

    suspend operator fun invoke(workoutName: String, sessionJson: String) {
        if (sessionJson.isNotEmpty()) {
            saveIncompleteSessionUseCase(workoutName, sessionJson)
        }
        addIncompleteWorkoutUseCase(workoutName)
    }
}
