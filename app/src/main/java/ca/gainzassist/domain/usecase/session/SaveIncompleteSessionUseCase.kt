package ca.gainzassist.domain.usecase.session

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class SaveIncompleteSessionUseCase(private val sessionPreferencesRepository: SessionPreferencesRepository) {

    suspend operator fun invoke(workoutName: String, sessionJson: String?) {
        if (workoutName.isNotBlank()) {
            sessionPreferencesRepository.saveIncompleteSession(workoutName, sessionJson)
        }
    }
}
