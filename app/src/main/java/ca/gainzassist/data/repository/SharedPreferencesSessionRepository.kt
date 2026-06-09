package ca.gainzassist.data.repository

import android.content.Context
import ca.gainzassist.core.coroutines.DispatcherProvider
import ca.gainzassist.domain.repository.SessionPreferencesRepository
import ca.gainzassist.util.Preferences
import kotlinx.coroutines.withContext

class SharedPreferencesSessionRepository(
    context: Context,
    private val dispatcherProvider: DispatcherProvider
) : SessionPreferencesRepository {

    private val appContext = context.applicationContext

    override suspend fun getIncompleteWorkoutNames(): Set<String> = withContext(dispatcherProvider.io) {
        Preferences.getIncompleteWorkouts(appContext)?.toSet() ?: emptySet()
    }

    override suspend fun addIncompleteWorkout(workoutName: String) = withContext(dispatcherProvider.io) {
        Preferences.addIncompleteWorkoutPref(appContext, workoutName)
    }

    override suspend fun removeIncompleteWorkout(workoutName: String): Boolean = withContext(dispatcherProvider.io) {
        Preferences.removeIncompleteWorkoutPref(appContext, workoutName)
    }

    override suspend fun saveIncompleteSession(workoutName: String, sessionJson: String?) = withContext(dispatcherProvider.io) {
        Preferences.addIncompleteSessionPref(appContext, workoutName, sessionJson)
    }

    override suspend fun getIncompleteSession(workoutName: String): String? = withContext(dispatcherProvider.io) {
        Preferences.getIncompleteSessionPref(appContext, workoutName)
    }

    override suspend fun removeIncompleteSession(workoutName: String) = withContext(dispatcherProvider.io) {
        Preferences.removeIncompleteSessionPref(appContext, workoutName)
    }

    override suspend fun saveSessionProgress(workoutName: String, progressJson: String?) = withContext(dispatcherProvider.io) {
        Preferences.addSessionProgressPref(appContext, workoutName, progressJson)
    }

    override suspend fun getSessionProgress(workoutName: String): String? = withContext(dispatcherProvider.io) {
        Preferences.getSessionProgressPref(appContext, workoutName)
    }

    override suspend fun removeSessionProgress(workoutName: String) = withContext(dispatcherProvider.io) {
        Preferences.removeSessionProgressPref(appContext, workoutName)
    }
}
