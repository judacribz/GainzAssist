package ca.judacribz.gainzassist.util.firebase;

import android.app.Activity;
import android.content.Intent;
import ca.judacribz.gainzassist.background.FirebaseService;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Workout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static ca.judacribz.gainzassist.util.Helper.*;

public class Database {

    // Constants
    // --------------------------------------------------------------------------------------------
    private final static String EMAIL = "email";
    private final static String USERNAME = "username";
    private final static String WORKOUTS = "workouts";
    private final static String SESSIONS = "sessions";
    private final static String SETS = "sets";

    private final static String DEFAULT_WORKOUTS_PATH = "default_workouts";
    private static final String USER_PATH = "users/%s";

    private static FirebaseUser firebaseUser;
    private static DatabaseReference
            userRef,
            userWorkoutsRef,
            userWorkoutSessionsRef;

    // --------------------------------------------------------------------------------------------

    //TODO change to get ref for a specific user when friends/chatting added
    /* Gets firebase db reference for 'users/<uid>/' */
    private static DatabaseReference getUserRef() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return (firebaseUser != null) ?
                firebaseDatabase.getReference(String.format(USER_PATH, firebaseUser.getUid())) :
                null;
    }

    /* Gets firebase db reference for 'users/<uid>/workouts/' */
    public static DatabaseReference getWorkoutsRef() {
        userRef = getUserRef();

        return (userRef != null) ? userRef.child(WORKOUTS) : null;
    }

    /* Gets firebase db reference for 'users/<uid>/sessions/' */
    public static DatabaseReference getWorkoutSessionsRef() {
        userRef = getUserRef();

        return (userRef != null) ? userRef.child(SESSIONS) : null;
    }


    /* Sets the user email in firebase db under 'users/<uid>/email' */
    public static void setUserInfo(final Activity act) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // FIREBASE: add user data
        if (firebaseUser != null) {
            userRef = getUserRef();

            if (userRef != null) {
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot userShot) {

                        // If newly added user
                        if (!userShot.hasChildren()) {
                            userRef.child(EMAIL).setValue(firebaseUser.getEmail());

                            // Copy default workouts from 'default_workouts/' to  'user/<uid>/workouts/'
                            copyDefaultWorkoutsFirebase();
                        }

                        if (!isMyServiceRunning(act, FirebaseService.class)) {
                            act.startService(new Intent(act, FirebaseService.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    /* Gets workouts from firebase db under 'default_workouts/' and adds it to
     * 'users/<uid>/workouts/' */
    private static void copyDefaultWorkoutsFirebase() {
        DatabaseReference defaultWorkoutsRef = FirebaseDatabase.getInstance().getReference(DEFAULT_WORKOUTS_PATH);
        userWorkoutsRef = getWorkoutsRef();

        if (defaultWorkoutsRef != null && userWorkoutsRef != null) {
            defaultWorkoutsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot defaultWorkoutsShot) {
                    userWorkoutsRef.setValue(defaultWorkoutsShot.getValue());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }


    /* Adds a workout under "users/<uid>/workouts/" */
    public static void addWorkoutFirebase(Workout workout) {
        userWorkoutsRef = getWorkoutsRef();

        if (userWorkoutsRef != null) {
            userWorkoutsRef.child(workout.getName()).setValue(workout.toMap());
        }
    }

    public static void addWorkoutSessionFirebase(Session session) {
        userWorkoutSessionsRef = getWorkoutSessionsRef();

        if (userWorkoutSessionsRef != null) {
            userWorkoutSessionsRef.child(String.valueOf(session.getTimestamp())).setValue(session.toMap());
        }
    }
    public static void deleteWorkoutFirebase(String workoutName) {
        userWorkoutsRef = getWorkoutsRef();

        if (userWorkoutsRef != null) {
            userWorkoutsRef.child(workoutName).removeValue();
        }
    }
}