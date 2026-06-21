package ca.gainzassist.data.local.preferences

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import ca.gainzassist.R
import ca.gainzassist.core.constants.AccountConst.EMAIL
import ca.gainzassist.core.constants.AccountConst.UID
import ca.gainzassist.core.constants.ExerciseConst.INCOMPLETE_WORKOUTS
import ca.gainzassist.core.constants.ExerciseConst.WORKOUT_EX_IND
import ca.gainzassist.core.constants.ExerciseConst.WORKOUT_PROGRESS

object Preferences {

    @JvmStatic
    fun getEmailPref(context: Context): String? = getSharedPref(context, R.string.file_user_info).getString(EMAIL, null)

    @JvmStatic
    fun setUserInfoPref(context: Context, email: String?, uid: String?) {
        getSharedPref(context, R.string.file_user_info).edit {
            putString(EMAIL, email)
            putString(UID, uid)
        }
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
        getSharedPref(context!!, R.string.file_settings_info).edit {
            putString("THEME", themeName)
        }
    }

    @JvmStatic
    fun getThemePref(context: Activity): String? = getSharedPref(
        context,
        R.string.file_settings_info
    ).getString("THEME", null)

    private fun addIncompleteWorkoutPref(context: Context, incompleteWorkouts: Set<String>) {
        getSharedPref(context, R.string.file_workout_info).edit {
            putStringSet(INCOMPLETE_WORKOUTS, incompleteWorkouts)
        }
    }

    @JvmStatic
    fun getIncompleteWorkouts(context: Context): MutableSet<String>? = getSharedPref(
        context,
        R.string.file_workout_info
    ).getStringSet(INCOMPLETE_WORKOUTS, null)

    @JvmStatic
    fun removeIncompleteWorkoutPref(context: Context, workoutName: String): Boolean {
        val incompleteWorkouts = getIncompleteWorkouts(context) ?: return false
        val removed = incompleteWorkouts.remove(workoutName)
        if (incompleteWorkouts.isEmpty()) {
            getSharedPref(context, R.string.file_workout_info).edit {
                remove(INCOMPLETE_WORKOUTS)
            }
        } else {
            addIncompleteWorkoutPref(context, incompleteWorkouts)
        }
        return removed
    }

    @JvmStatic
    fun addIncompleteSessionPref(context: Context, workoutName: String, sessionJson: String?) {
        getSharedPref(context, R.string.file_workout_info).edit {
            putString(String.format(WORKOUT_EX_IND, workoutName), sessionJson)
        }
    }

    @JvmStatic
    fun getIncompleteSessionPref(context: Context, workoutName: String): String? = getSharedPref(
        context,
        R.string.file_workout_info
    ).getString(String.format(WORKOUT_EX_IND, workoutName), null)

    @JvmStatic
    fun removeIncompleteSessionPref(context: Context, workoutName: String) {
        getSharedPref(context, R.string.file_workout_info).edit {
            remove(String.format(WORKOUT_EX_IND, workoutName))
        }
    }

    @JvmStatic
    fun addSessionProgressPref(context: Context?, workoutName: String, progressJson: String?) {
        getSharedPref(context!!, R.string.file_workout_info).edit {
            putString(String.format(WORKOUT_PROGRESS, workoutName), progressJson)
        }
    }

    @JvmStatic
    fun getSessionProgressPref(context: Context?, workoutName: String): String? = getSharedPref(
        context!!,
        R.string.file_workout_info
    ).getString(String.format(WORKOUT_PROGRESS, workoutName), null)

    @JvmStatic
    fun removeSessionProgressPref(context: Context?, workoutName: String) {
        getSharedPref(context!!, R.string.file_workout_info).edit {
            remove(String.format(WORKOUT_PROGRESS, workoutName))
        }
    }

    @JvmStatic
    fun getSharedPref(context: Context, fileId: Int): SharedPreferences = context.getSharedPreferences(
        context.getString(fileId),
        Context.MODE_PRIVATE
    )
}
