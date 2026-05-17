package ca.judacribz.gainzassist.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.constants.AccountConst.EMAIL
import ca.judacribz.gainzassist.constants.ExerciseConst.INCOMPLETE_WORKOUTS
import ca.judacribz.gainzassist.constants.ExerciseConst.WORKOUT_EX_IND
import ca.judacribz.gainzassist.constants.ExerciseConst.WORKOUT_PROGRESS
import java.util.*

object Preferences {

    @JvmStatic
    fun getEmailPref(context: Context): String? {
        return getSharedPref(context, R.string.file_user_info).getString(EMAIL, null)
    }

    @JvmStatic
    fun setUserInfoPref(context: Context, email: String?, uid: String?) {
        val editor = getSharedPref(context, R.string.file_user_info).edit()
        editor.putString(EMAIL, email)
        editor.apply()
    }

    @JvmStatic
    fun addIncompleteWorkoutPref(context: Context, workoutName: String) {
        var incompleteWorkouts = getIncompleteWorkouts(context)
        if (incompleteWorkouts == null) {
            incompleteWorkouts = HashSet()
        }
        incompleteWorkouts.add(workoutName)
        addIncompleteWorkoutPref(context, incompleteWorkouts)
    }

    @JvmStatic
    fun setTheme(context: Context?, themeName: String?) {
        val editor = getSharedPref(context!!, R.string.file_settings_info).edit()
        editor.putString("THEME", themeName)
        editor.apply()
    }

    @JvmStatic
    fun getThemePref(context: Activity): String? {
        return getSharedPref(
            context,
            R.string.file_settings_info
        ).getString("THEME", null)
    }

    private fun addIncompleteWorkoutPref(context: Context, incompleteWorkouts: Set<String>) {
        val editor = getSharedPref(context, R.string.file_workout_info).edit()
        editor.putStringSet(INCOMPLETE_WORKOUTS, incompleteWorkouts)
        editor.apply()
    }

    @JvmStatic
    fun getIncompleteWorkouts(context: Context): MutableSet<String>? {
        return getSharedPref(
            context,
            R.string.file_workout_info
        ).getStringSet(INCOMPLETE_WORKOUTS, null)
    }

    @JvmStatic
    fun removeIncompleteWorkoutPref(context: Context, workoutName: String): Boolean {
        val incompleteWorkouts = getIncompleteWorkouts(context) ?: return false
        val removed = incompleteWorkouts.remove(workoutName)
        if (incompleteWorkouts.size == 0) {
            val editor = getSharedPref(context, R.string.file_workout_info).edit()
            editor.remove(INCOMPLETE_WORKOUTS)
            editor.apply()
        } else {
            addIncompleteWorkoutPref(context, incompleteWorkouts)
        }
        return removed
    }

    @JvmStatic
    fun addIncompleteSessionPref(context: Context, workoutName: String, sessionJson: String?) {
        val editor = getSharedPref(context, R.string.file_workout_info).edit()
        editor.putString(String.format(WORKOUT_EX_IND, workoutName), sessionJson)
        editor.apply()
    }

    @JvmStatic
    fun getIncompleteSessionPref(context: Context, workoutName: String): String? {
        return getSharedPref(
            context,
            R.string.file_workout_info
        ).getString(String.format(WORKOUT_EX_IND, workoutName), null)
    }

    @JvmStatic
    fun removeIncompleteSessionPref(context: Context, workoutName: String) {
        val editor = getSharedPref(context, R.string.file_workout_info).edit()
        editor.remove(String.format(WORKOUT_EX_IND, workoutName))
        editor.apply()
    }

    @JvmStatic
    fun addSessionProgressPref(context: Context?, workoutName: String, progressJson: String?) {
        val editor = getSharedPref(context!!, R.string.file_workout_info).edit()
        editor.putString(String.format(WORKOUT_PROGRESS, workoutName), progressJson)
        editor.apply()
    }

    @JvmStatic
    fun getSessionProgressPref(context: Context?, workoutName: String): String? {
        return getSharedPref(
            context!!,
            R.string.file_workout_info
        ).getString(String.format(WORKOUT_PROGRESS, workoutName), null)
    }

    @JvmStatic
    fun removeSessionProgressPref(context: Context?, workoutName: String) {
        val editor = getSharedPref(context!!, R.string.file_workout_info).edit()
        editor.remove(String.format(WORKOUT_PROGRESS, workoutName))
        editor.apply()
    }

    @JvmStatic
    fun getSharedPref(context: Context, fileId: Int): SharedPreferences {
        return context.getSharedPreferences(
            context.getString(fileId),
            Context.MODE_PRIVATE
        )
    }
}
