package ca.judacribz.gainzassist.async;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import ca.judacribz.gainzassist.Main;

import static ca.judacribz.gainzassist.firebase.Database.setUserInfo;

public class BGTask extends AsyncTask<Object, Void, Object[]> {

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
