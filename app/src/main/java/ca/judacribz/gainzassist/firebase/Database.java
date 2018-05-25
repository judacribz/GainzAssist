package ca.judacribz.gainzassist.firebase;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.User;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.WorkoutHelper;

public class Database {

    // Constants
    // --------------------------------------------------------------------------------------------
    private final static String EMAIL = "email";
    private final static String USERNAME = "username";
    private final static String WORKOUTS = "workouts";
    private final static String SETS = "sets";

    public final static String DEFAULT_WORKOUTS_PATH = "default_workouts";
    public static final String USER_PATH = "users/%s";
    public static final String WORKOUTS_PATH = "users/%s/workouts";

    // --------------------------------------------------------------------------------------------


    /* Gets firebase db reference for 'users/<uid>/' */
    public static DatabaseReference getUserRef() {
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

                    // Sets singleton User Instance variables
                    User user = User.getInstance();
                    user.setEmail(email);
                    user.setUid(uid);

                    // If newly added user
                    if (!dataSnapshot.hasChildren()) {
                        userRef
                            .child(EMAIL)
                            .setValue(email);

                        // Add default workouts under 'user/<uid>/workouts/'
                        addDefaultWorkoutsFirebase(act);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /* Gets all workouts from firebase db from 'users/<uid>/workouts/' or from func arg 'ref' if
     * provided */
    public static void getWorkoutsFirebase(final Activity act, @Nullable String ref) {
        DatabaseReference workoutRef;

        if (ref == null) {
            workoutRef = getWorkoutsRef();
        } else {
            workoutRef = FirebaseDatabase.getInstance().getReference(ref);
        }

        workoutRef.addChildEventListener(new ChildEventListener() {
            WorkoutHelper workoutHelper = new WorkoutHelper(act);
            ArrayList<Exercise> exercises = new ArrayList<>();
            ArrayList<Set> sets = new ArrayList<>();
            Exercise exercise;
            Set set;


            @Override
            public void onChildAdded(DataSnapshot workoutShot, String s) {
                for (DataSnapshot exerciseShot : workoutShot.getChildren()) {

                    // Add set to sets list
                    for (DataSnapshot setShot : exerciseShot.child(SETS).getChildren()) {
                        set = setShot.getValue(Set.class);

                        if (set != null) {
                            set.setSetNumber(Integer.valueOf(setShot.getKey()));

                            sets.add(set);
                        }
                    }

                    // Adds sets to exercise object, and add exercise to exercises list
                    exercise = exerciseShot.getValue(Exercise.class);
                    if (exercise != null) {
                        exercise.setName(exerciseShot.getKey());
                        exercise.setSets(sets);

                        exercises.add(exercise);
                    }
                    sets.clear();
                }

                // Add workout name and exercises list to workouts list
                workoutHelper.addWorkout(new Workout(workoutShot.getKey(), exercises));
                exercises.clear();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /* Gets workouts from firebase db under 'default_workouts/' and adds it to
     * 'users/<uid>/workouts/' */
    public static void addDefaultWorkoutsFirebase(Activity act) {
        getWorkoutsFirebase(act, DEFAULT_WORKOUTS_PATH);

        addWorkoutsFirebase(act, null);
    }

    /* Adds provided workouts under "users/<uid>/workouts/" and adds missing workouts in local db */
    public static void addWorkoutsFirebase(Activity act,@Nullable ArrayList<Workout> workouts) {
        WorkoutHelper workoutHelper = new WorkoutHelper(act);
        ArrayList<String> workoutNames = workoutHelper.getAllWorkoutNames();
        ArrayList<Workout> workoutsToAdd = new ArrayList<>();

        if (workouts == null) {
            workouts = workoutHelper.getAllWorkouts();
        }

        for (Workout workout : workouts) {
            addWorkoutFirebase(workout);
            if (!workoutNames.contains(workout.getName())) {
                workoutsToAdd.add(workout);
            }
        }

        if (!workoutsToAdd.isEmpty()) {
            workoutHelper.addWorkouts(workoutsToAdd);
        }
    }

    /* Adds a workout under "users/<uid>/workouts/" */
    public static void addWorkoutFirebase(Workout workout) {

        getWorkoutsRef()
                .child(workout.getName()).setValue(workout.toMap())
        ;


    }
}