package ca.gainzassist.domain.usecase.user

import ca.gainzassist.domain.repository.UserPreferencesRepository

class SetThemeUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(themeName: String?) {
        userPreferencesRepository.setTheme(themeName)
    }
}
