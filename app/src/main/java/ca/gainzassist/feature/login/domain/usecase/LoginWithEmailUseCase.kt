package ca.gainzassist.feature.login.domain.usecase

import ca.gainzassist.feature.login.domain.repository.LoginRepository

class LoginWithEmailUseCase(
    private val loginRepository: LoginRepository
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return loginRepository.loginWithEmail(email, password)
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return loginRepository.signUpWithEmail(email, password)
    }
}
