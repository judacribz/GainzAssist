package ca.gainzassist.feature.login.domain.usecase

import ca.gainzassist.feature.login.domain.repository.LoginRepository

class SignOutUseCase(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return loginRepository.signOut()
    }
}
