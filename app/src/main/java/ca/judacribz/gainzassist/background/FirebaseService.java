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
                public void onChildAdded(@NonNull final DataSnapshot workoutShot, String s) {
                    String idStr = String.valueOf(workoutShot.child("id").getValue());

                    workoutRepo.workoutExists(Long.valueOf(idStr)).observeForever(new Observer<Long>() {
                        @Override
                        public void onChanged(@Nullable Long id) {
                            if (id == null) {
                                workoutRepo.insertWorkout(extractWorkout(workoutShot));
                            }
                        }
                    });

                    //TODO change to read from textfile for list of current workout names instead of using interface
                }

                @Override
                public void onChildChanged(DataSnapshot workoutShot, String s) {
                    workoutRepo.updateWorkout(extractWorkout(workoutShot));
                }

                @Override
                public void onChildRemoved(DataSnapshot workoutShot) {
                    Toast.makeText(FirebaseService.this, "Deleted " + workoutShot.getKey(), Toast.LENGTH_SHORT).show();
                    workoutRepo.deleteWorkout(extractWorkout(workoutShot));
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
                    workoutRepo.insertSession(extractSession(sessionShot), false);
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
