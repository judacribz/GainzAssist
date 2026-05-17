package ca.judacribz.gainzassist.background;

import android.app.IntentService;
import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;
import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener;
import ca.judacribz.gainzassist.models.*;
import ca.judacribz.gainzassist.models.db.WorkoutRepo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.orhanobut.logger.Logger;

import static ca.judacribz.gainzassist.util.Misc.extractSession;
import static ca.judacribz.gainzassist.util.Misc.extractWorkout;
import static ca.judacribz.gainzassist.util.firebase.Database.getWorkoutSessionsRef;
import static ca.judacribz.gainzassist.util.firebase.Database.getWorkoutsRef;


public class FirebaseService extends IntentService {

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
                public void onChildAdded(@NonNull final DataSnapshot workoutShot, String s) {
                    workoutRepo.insertWorkout(extractWorkout(workoutShot));
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot workoutShot, String s) {
                    workoutRepo.updateWorkout(extractWorkout(workoutShot));
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot workoutShot) {
                    Toast.makeText(FirebaseService.this, "Deleted " + workoutShot.getKey(), Toast.LENGTH_SHORT).show();
                    workoutRepo.deleteWorkout(extractWorkout(workoutShot));
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Logger.d("FIREBASE DB WORKOUT ERROR: " + databaseError.getMessage());
                }
            });
        }

        if (userSessionRef != null) {
            userSessionRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot sessionShot, String s) {
                    workoutRepo.insertSession(extractSession(sessionShot), false);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot sessionShot, String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot sessionShot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot sessionShot, String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Logger.d("FIREBASE DB SESSION ERROR: " + databaseError.getMessage());
                }
            });
        }

        return Service.START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
