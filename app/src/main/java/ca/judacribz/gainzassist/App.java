package ca.judacribz.gainzassist;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // debugging
        Logger.addLogAdapter(new AndroidLogAdapter());

        //
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


    }

}