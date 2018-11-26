package ca.judacribz.gainzassist.background;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;
import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener;
import ca.judacribz.gainzassist.models.*;
import ca.judacribz.gainzassist.models.db.WorkoutRepo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import static ca.judacribz.gainzassist.util.Misc.extractSession;
import static ca.judacribz.gainzassist.util.firebase.Database.getWorkoutSessionsRef;
import static ca.judacribz.gainzassist.util.firebase.Database.getWorkoutsRef;


public class FirebaseService extends IntentService implements
        OnWorkoutReceivedListener {

    WorkoutRepo workoutRepo;

    public FirebaseService() {
        super("FirebaseService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        workoutRepo = new WorkoutRepo(getApplication());
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        DatabaseReference userWorkoutsRef = getWorkoutsRef();
        DatabaseReference userSessionRef = getWorkoutSessionsRef();

        if (userWorkoutsRef != null) {
            userWorkoutsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot workoutShot, String s) {

                    //TODO change to read from textfile for list of current workout names instead of using interface
                    workoutRepo.getWorkoutId(FirebaseService.this, workoutShot);
                }

                @Override
                public void onChildChanged(DataSnapshot workoutShot, String s) {
                    //TODO make this work
//                    workoutRepo.updateWorkout(extractWorkout(workoutShot));
                }

                @Override
                public void onChildRemoved(DataSnapshot workoutShot) {
                    Toast.makeText(FirebaseService.this, "Deleted " + workoutShot.getKey(), Toast.LENGTH_SHORT).show();
                    workoutRepo.deleteWorkout(workoutShot.getKey());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        if (userSessionRef != null) {
            userSessionRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot sessionShot, String s) {
                    extractSession(sessionShot);
                }

                @Override
                public void onChildChanged(DataSnapshot sessionShot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot sessionShot) {
                }

                @Override
                public void onChildMoved(DataSnapshot sessionShot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        return Service.START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }


    @Override
    public void onWorkoutsReceived(Workout workout) {
        workoutRepo.insertWorkout(workout);
    }
}