package ca.judacribz.gainzassist

import android.app.Application
import com.facebook.appevents.AppEventsLogger
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        validateSecrets()
        Logger.addLogAdapter(AndroidLogAdapter())
        
        // Facebook SDK auto-initializes if App ID and Client Token are in manifest.
        // We only need to activate app events.
        AppEventsLogger.activateApp(this)
    }

    private fun validateSecrets() {
        if (BuildConfig.DEFAULT_WEB_CLIENT_ID.isBlank() ||
            BuildConfig.FACEBOOK_APP_ID.isBlank() ||
            BuildConfig.FACEBOOK_CLIENT_TOKEN.isBlank() ||
            BuildConfig.FB_LOGIN_PROTOCOL_SCHEME.isBlank() ||
            BuildConfig.GOOGLE_API_KEY.isBlank()
        ) {
            val errorMsg = "CRITICAL: Missing configuration in secrets.properties. " +
                    "Please ensure all required keys are provided for auth and video search to work."
            if (BuildConfig.DEBUG) {
                // Show a toast or log loudly in debug
                android.widget.Toast.makeText(this, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                Logger.e(errorMsg)
            } else {
                // Fail loudly in release
                throw IllegalStateException(errorMsg)
            }
        }
    }
}
