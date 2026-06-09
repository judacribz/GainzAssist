package ca.gainzassist.domain.usecase.session

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class RemoveSessionProgressUseCase(
    private val sessionPreferencesRepository: SessionPreferencesRepository
) {
    suspend operator fun invoke(workoutName: String) {
        if (workoutName.isNotBlank()) {
            sessionPreferencesRepository.removeSessionProgress(workoutName)
        }
    }
}
