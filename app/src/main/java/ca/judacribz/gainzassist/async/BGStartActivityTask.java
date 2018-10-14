package ca.judacribz.gainzassist.async;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import ca.judacribz.gainzassist.Main;
import ca.judacribz.gainzassist.models.WorkoutHelper;

import static ca.judacribz.gainzassist.firebase.Database.setUserInfo;
import static java.lang.Thread.sleep;

public class BGStartActivityTask extends AsyncTask<Object, Void, Object[]> {

    @Override
    protected Object[] doInBackground(Object... obj) {
        setUserInfo((Activity) obj[0]);

        return obj;
    }

    @Override
    protected void onPostExecute(final Object[] objs) {
        final Activity act = (Activity) objs[0];
        final WorkoutHelper workoutHelper = new WorkoutHelper(act);

        while (!workoutHelper.exists()) {
            try {
                sleep(2000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        act.startActivity((Intent) objs[1]);
        act.finish();

    }
}
