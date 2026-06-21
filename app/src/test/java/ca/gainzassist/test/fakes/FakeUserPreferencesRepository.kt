package ca.gainzassist.test.fakes

import ca.gainzassist.domain.repository.UserPreferencesRepository

class FakeUserPreferencesRepository : UserPreferencesRepository {
    var currentEmail: String? = null
    var currentUid: String? = null
    var currentTheme: String? = null

    override suspend fun getEmail(): String? = currentEmail

    override suspend fun setUserInfo(email: String?, uid: String?) {
        currentEmail = email
        currentUid = uid
    }

    override suspend fun getTheme(): String? = currentTheme

    override suspend fun setTheme(themeName: String?) {
        currentTheme = themeName
    }
}
