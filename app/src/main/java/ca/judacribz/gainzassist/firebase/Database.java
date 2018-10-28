package ca.judacribz.gainzassist.firebase;

import android.app.Activity;

import java.util.ArrayList;

import android.content.Intent;
import android.widget.Toast;
import ca.judacribz.gainzassist.async.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ca.judacribz.gainzassist.models.CurrUser;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.WorkoutHelper;

public class Database {

    // Constants
    // --------------------------------------------------------------------------------------------
    private final static String EMAIL = "email";
    private final static String USERNAME = "username";
    private final static String WORKOUTS = "workouts";
    private final static String SETS = "sets";

    private final static String DEFAULT_WORKOUTS_PATH = "default_workouts";
    private static final String USER_PATH = "users/%s";

    // --------------------------------------------------------------------------------------------

    /* Gets firebase db reference for 'users/<uid>/' */
    private static DatabaseReference getUserRef() {
        DatabaseReference userRef = null;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference(
                    String.format(USER_PATH, user.getUid())
            );
        }

        return userRef;
    }

    /* Gets firebase db reference for 'users/<uid>/workouts/' */
    public static DatabaseReference getWorkoutsRef() {
        return getUserRef().child(WORKOUTS);
    }


    /* Sets the user email in firebase db under 'users/<uid>/email' */
    public static void setUserInfo(final Activity act) {
        final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        // FIREBASE: add user data
        if (fbUser != null) {
            final DatabaseReference userRef = getUserRef();

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String email = fbUser.getEmail();
                    String uid = fbUser.getUid();

                    // Sets singleton CurrUser Instance variables
                    CurrUser user = CurrUser.getInstance();
                    user.setEmail(email);
                    user.setUid(uid);

                    act.startService(new Intent(act, FirebaseService.class));

                    // If newly added user
                    if (!dataSnapshot.hasChildren()) {
                        userRef
                            .child(EMAIL)
                            .setValue(email);

                        // Copy default workouts from 'default_workouts/' to  'user/<uid>/workouts/'
                        copyDefaultWorkoutsFirebase();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    /* Gets workouts from firebase db under 'default_workouts/' and adds it to
     * 'users/<uid>/workouts/' */
    public static void copyDefaultWorkoutsFirebase() {
        DatabaseReference defaultRef = FirebaseDatabase.getInstance().getReference(
                DEFAULT_WORKOUTS_PATH
        );

        defaultRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getWorkoutsRef().setValue(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    /* Adds a workout under "users/<uid>/workouts/" */
    public static void addWorkoutFirebase(Workout workout) {
        getWorkoutsRef().child(workout.getName()).setValue(workout.toMap());
    }

    public static void deleteWorkoutFirebase(String workoutName) {
        getWorkoutsRef().child(workoutName).removeValue();
    }
}