package ca.judacribz.gainzassist.async;

import ca.judacribz.gainzassist.models.Workout;
import com.google.firebase.database.DataSnapshot;

public interface OnWorkoutReceivedListener {
    void onWorkoutsReceived(Workout workout);
    void onWorkoutShotReceived(DataSnapshot workoutShot);
}