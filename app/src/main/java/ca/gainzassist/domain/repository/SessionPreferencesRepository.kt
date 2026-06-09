package ca.gainzassist.domain.repository

interface SessionPreferencesRepository {
    suspend fun getIncompleteWorkoutNames(): Set<String>
    suspend fun addIncompleteWorkout(workoutName: String)
    suspend fun removeIncompleteWorkout(workoutName: String): Boolean

    suspend fun saveIncompleteSession(workoutName: String, sessionJson: String?)
    suspend fun getIncompleteSession(workoutName: String): String?
    suspend fun removeIncompleteSession(workoutName: String)

    suspend fun saveSessionProgress(workoutName: String, progressJson: String?)
    suspend fun getSessionProgress(workoutName: String): String?
    suspend fun removeSessionProgress(workoutName: String)
}
