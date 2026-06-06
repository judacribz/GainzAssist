package ca.gainzassist.domain.usecase.session

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class SaveSessionProgressUseCase(
    private val sessionPreferencesRepository: SessionPreferencesRepository
) {
    suspend operator fun invoke(workoutName: String, progressJson: String?) {
        if (workoutName.isNotBlank()) {
            sessionPreferencesRepository.saveSessionProgress(workoutName, progressJson)
        }
    }
}
