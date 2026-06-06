package ca.gainzassist.data.repository

import android.content.Context
import ca.gainzassist.R
import ca.gainzassist.core.coroutines.DispatcherProvider
import ca.gainzassist.domain.repository.UserPreferencesRepository
import ca.gainzassist.util.Preferences
import kotlinx.coroutines.withContext

class SharedPreferencesUserRepository(
    context: Context,
    private val dispatcherProvider: DispatcherProvider
) : UserPreferencesRepository {

    private val appContext = context.applicationContext

    override suspend fun getEmail(): String? = withContext(dispatcherProvider.io) {
        Preferences.getEmailPref(appContext)
    }

    override suspend fun setUserInfo(email: String?, uid: String?) = withContext(dispatcherProvider.io) {
        Preferences.setUserInfoPref(appContext, email, uid)
    }

    override suspend fun getTheme(): String? = withContext(dispatcherProvider.io) {
        Preferences.getSharedPref(appContext, R.string.file_settings_info).getString("THEME", null)
    }

    override suspend fun setTheme(themeName: String?) = withContext(dispatcherProvider.io) {
        Preferences.setTheme(appContext, themeName)
    }
}
