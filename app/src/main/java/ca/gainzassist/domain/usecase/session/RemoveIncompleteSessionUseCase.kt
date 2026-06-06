package ca.gainzassist.domain.usecase.session

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class RemoveIncompleteSessionUseCase(
    private val sessionPreferencesRepository: SessionPreferencesRepository
) {
    suspend operator fun invoke(workoutName: String) {
        if (workoutName.isNotBlank()) {
            sessionPreferencesRepository.removeIncompleteSession(workoutName)
        }
    }
}
