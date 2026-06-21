package ca.gainzassist.domain.usecase.session

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class GetIncompleteWorkoutNamesUseCase(
    private val sessionPreferencesRepository: SessionPreferencesRepository
) {
    suspend operator fun invoke(): Set<String> = sessionPreferencesRepository.getIncompleteWorkoutNames()
}
