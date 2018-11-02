package ca.judacribz.gainzassist.async;

import ca.judacribz.gainzassist.models.Workout;

public interface OnWorkoutReceivedListener {

    void onWorkoutsReceived(Workout workout);
}
