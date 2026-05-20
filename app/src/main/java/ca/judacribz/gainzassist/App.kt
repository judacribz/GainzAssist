package ca.judacribz.gainzassist

import android.app.Application
import com.facebook.appevents.AppEventsLogger
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        
        // Facebook SDK auto-initializes if App ID and Client Token are in manifest.
        // We only need to activate app events.
        AppEventsLogger.activateApp(this)
    }
}
