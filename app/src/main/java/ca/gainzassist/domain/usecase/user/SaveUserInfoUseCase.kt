package ca.gainzassist.domain.usecase.user

import ca.gainzassist.domain.repository.UserPreferencesRepository

class SaveUserInfoUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(email: String?, uid: String?) {
        userPreferencesRepository.setUserInfo(email, uid)
    }
}
