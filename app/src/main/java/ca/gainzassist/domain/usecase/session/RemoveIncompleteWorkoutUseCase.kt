package ca.gainzassist.domain.usecase.session

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class RemoveIncompleteWorkoutUseCase(private val sessionPreferencesRepository: SessionPreferencesRepository) {

    suspend operator fun invoke(workoutName: String): Boolean {
        if (workoutName.isBlank()) return false
        return sessionPreferencesRepository.removeIncompleteWorkout(workoutName)
    }
}
