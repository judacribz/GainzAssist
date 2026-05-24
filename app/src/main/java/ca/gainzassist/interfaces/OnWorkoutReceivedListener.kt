package ca.gainzassist.interfaces

import ca.gainzassist.models.Workout

interface OnWorkoutReceivedListener {
    fun onWorkoutsReceived(workout: Workout)
}
