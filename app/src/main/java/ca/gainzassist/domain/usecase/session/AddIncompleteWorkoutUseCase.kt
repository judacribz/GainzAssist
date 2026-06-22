package ca.gainzassist.domain.usecase.session

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class AddIncompleteWorkoutUseCase(private val sessionPreferencesRepository: SessionPreferencesRepository) {

    suspend operator fun invoke(workoutName: String) {
        if (workoutName.isNotBlank()) {
            sessionPreferencesRepository.addIncompleteWorkout(workoutName)
        }
    }
}
