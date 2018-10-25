package ca.judacribz.gainzassist.async;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import ca.judacribz.gainzassist.activities.authentication.Login;
import static ca.judacribz.gainzassist.firebase.Database.setUserInfo;

public class BGStartActivityTask extends AsyncTask<Object, Void, Object[]> {

    public BGStartActivityTask(z login) {
    }

    @Override
    protected Object[] doInBackground(Object... objs) {
        setUserInfo((Activity) objs[0]);

        return objs;
    }

    @Override
    protected void onPostExecute(final Object[] objs) {
        Activity act = (Activity) objs[0];
        act.startActivity((Intent) objs[1]);
        act.finish();

    }
}
