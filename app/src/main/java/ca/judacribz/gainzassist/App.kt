package ca.judacribz.gainzassist

import android.app.Application
import android.widget.Toast
import com.facebook.appevents.AppEventsLogger
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        
        val isFacebookEnabled = BuildConfig.ENABLE_FACEBOOK_LOGIN.toBooleanStrictOrNull() ?: true

        if (validateSecrets(isFacebookEnabled)) {
            // Facebook SDK auto-initializes if App ID and Client Token are in manifest.
            // We only need to activate app events.
            if (isFacebookEnabled) {
                AppEventsLogger.activateApp(this)
            }
        }
    }

    /**
     * @return true if secrets are valid, false if invalid but allowed to proceed (debug only)
     * @throws IllegalStateException if secrets are invalid in release
     */
    private fun validateSecrets(isFacebookEnabled: Boolean): Boolean {
        val missingFacebook = isFacebookEnabled && (BuildConfig.FACEBOOK_APP_ID.isBlank() ||
                BuildConfig.FACEBOOK_CLIENT_TOKEN.isBlank() ||
                BuildConfig.FB_LOGIN_PROTOCOL_SCHEME.isBlank())
        
        val missingGoogle = BuildConfig.GOOGLE_API_KEY.isBlank()
        
        if (missingFacebook || missingGoogle) {
            val errorMsg = "CRITICAL: Missing configuration in secrets.properties. Please ensure " +
                    "all required keys are provided for auth and video search to work."
            
            if (BuildConfig.DEBUG) {
                // Show a toast or log loudly in debug
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                Logger.e(errorMsg)
                
                if (missingFacebook) {
                    Logger.w("Facebook configuration missing. Skipping Facebook App events activation.")
                }
                
                return !missingFacebook
            } else {
                // Fail loudly in release
                throw IllegalStateException(errorMsg)
            }
        }
        return true
    }
}
