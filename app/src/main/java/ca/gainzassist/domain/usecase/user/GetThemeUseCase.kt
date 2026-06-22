package ca.gainzassist.domain.usecase.user

import ca.gainzassist.domain.repository.UserPreferencesRepository

class GetThemeUseCase(private val userPreferencesRepository: UserPreferencesRepository) {

    suspend operator fun invoke(): String? = userPreferencesRepository.getTheme()
}
