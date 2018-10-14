package ca.judacribz.gainzassist.async;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import ca.judacribz.gainzassist.Main;

import static ca.judacribz.gainzassist.firebase.Database.setUserInfo;

public class BGStartActivityTask extends AsyncTask<Object, Void, Object[]> {

    @Override
    protected Object[] doInBackground(Object... obj) {
        setUserInfo((Activity) obj[0]);

        return obj;
    }


    @Override
    protected void onPostExecute(Object[] objs) {
        Activity act = (Activity) objs[0];

        act.startActivity((Intent) objs[1]);
        act.finish();
    }
}
