package ca.gainzassist.test.fakes

import ca.gainzassist.domain.repository.SessionPreferencesRepository

class FakeSessionPreferencesRepository : SessionPreferencesRepository {
    private val incompleteWorkouts = mutableSetOf<String>()
    private val incompleteSessions = mutableMapOf<String, String>()
    private val sessionProgresses = mutableMapOf<String, String>()

    override suspend fun getIncompleteWorkoutNames(): Set<String> {
        return incompleteWorkouts.toSet()
    }

    override suspend fun addIncompleteWorkout(workoutName: String) {
        incompleteWorkouts.add(workoutName)
    }

    override suspend fun removeIncompleteWorkout(workoutName: String): Boolean {
        return incompleteWorkouts.remove(workoutName)
    }

    override suspend fun saveIncompleteSession(workoutName: String, sessionJson: String?) {
        if (sessionJson == null) {
            incompleteSessions.remove(workoutName)
        } else {
            incompleteSessions[workoutName] = sessionJson
        }
    }

    override suspend fun getIncompleteSession(workoutName: String): String? {
        return incompleteSessions[workoutName]
    }

    override suspend fun removeIncompleteSession(workoutName: String) {
        incompleteSessions.remove(workoutName)
    }

    override suspend fun saveSessionProgress(workoutName: String, progressJson: String?) {
        if (progressJson == null) {
            sessionProgresses.remove(workoutName)
        } else {
            sessionProgresses[workoutName] = progressJson
        }
    }

    override suspend fun getSessionProgress(workoutName: String): String? {
        return sessionProgresses[workoutName]
    }

    override suspend fun removeSessionProgress(workoutName: String) {
        sessionProgresses.remove(workoutName)
    }
}
