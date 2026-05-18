package ca.judacribz.gainzassist.interfaces

import ca.judacribz.gainzassist.models.Workout

interface OnWorkoutReceivedListener {
    fun onWorkoutsReceived(workout: Workout)
}
