package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;

import ca.judacribz.gainzassist.R;

public class Helper {
    private static final String EMAIL = "email";


    public static boolean isMyServiceRunning(Activity act, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getEmailFromPref(Context context) {
        return context.getSharedPreferences(context.getString(R.string.file_user_info), Context.MODE_PRIVATE).getString(EMAIL, null);
    }

    public static void setUserInfoInPref(Activity act, String email, String uid) {
        SharedPreferences sharedPref = act.getSharedPreferences(act.getString(R.string.file_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(EMAIL, email);
        editor.apply();

    }
}
