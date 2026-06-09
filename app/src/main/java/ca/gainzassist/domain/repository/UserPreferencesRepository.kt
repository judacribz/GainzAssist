package ca.gainzassist.domain.repository

interface UserPreferencesRepository {
    suspend fun getEmail(): String?
    suspend fun setUserInfo(email: String?, uid: String?)
    suspend fun getTheme(): String?
    suspend fun setTheme(themeName: String?)
}
