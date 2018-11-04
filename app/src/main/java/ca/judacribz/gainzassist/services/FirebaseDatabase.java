package ca.judacribz.gainzassist.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;
import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener;
import ca.judacribz.gainzassist.models.*;
import ca.judacribz.gainzassist.models.db.WorkoutRepo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;

import static ca.judacribz.gainzassist.firebase.Database.getWorkoutsRef;
import static ca.judacribz.gainzassist.util.Helper.extractWorkout;


public class FirebaseDatabase extends IntentService implements
        ChildEventListener,
        OnWorkoutReceivedListener {

    WorkoutRepo workoutRepo;

    public FirebaseDatabase() {
        super("FirebaseDatabase");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        workoutRepo = new WorkoutRepo(getApplication());
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        DatabaseReference userWorkoutsRef = getWorkoutsRef();

        if (userWorkoutsRef != null) {
            userWorkoutsRef.addChildEventListener(this);
        }

        return Service.START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public void onChildAdded(DataSnapshot workoutShot, String s) {

        //TODO change to read from textfile for list of current workout names instead of using interface
        workoutRepo.getWorkoutId(this, workoutShot);
    }

    @Override
    public void onChildChanged(DataSnapshot workoutShot, String s) {

        workoutRepo.insertWorkout(extractWorkout(workoutShot));

    }

    @Override
    public void onChildRemoved(DataSnapshot workoutShot) {
        Toast.makeText(this, "Deleted " + workoutShot.getKey(), Toast.LENGTH_SHORT).show();
        workoutRepo.deleteWorkout(workoutShot.getKey());
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }


    @Override
    public void onWorkoutsReceived(Workout workout) {
        workoutRepo.insertWorkout(workout);
    }
}
