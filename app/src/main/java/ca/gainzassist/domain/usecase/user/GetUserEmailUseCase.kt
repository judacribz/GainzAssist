package ca.gainzassist.domain.usecase.user

import ca.gainzassist.domain.repository.UserPreferencesRepository

class GetUserEmailUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(): String? {
        return userPreferencesRepository.getEmail()
    }
}
