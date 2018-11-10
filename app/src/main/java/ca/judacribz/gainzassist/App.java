package ca.judacribz.gainzassist;

import android.app.Application;
import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.firebase.database.FirebaseDatabase;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bugfender.init(this, "xsyWtQLUg8fbojf8MZJ5ZQjf9ThDDASY", BuildConfig.DEBUG);
        Bugfender.enableLogcatLogging();
        Bugfender.enableUIEventLogging(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}