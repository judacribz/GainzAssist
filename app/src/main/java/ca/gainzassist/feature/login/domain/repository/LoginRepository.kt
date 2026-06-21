package ca.gainzassist.feature.login.domain.repository

interface LoginRepository {
    suspend fun loginWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
