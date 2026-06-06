package ca.gainzassist.domain.usecase.session

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class GetSessionProgressUseCase(
    private val sessionPreferencesRepository: SessionPreferencesRepository
) {
    suspend operator fun invoke(workoutName: String): String? {
        if (workoutName.isBlank()) return null
        return sessionPreferencesRepository.getSessionProgress(workoutName)
    }
}
