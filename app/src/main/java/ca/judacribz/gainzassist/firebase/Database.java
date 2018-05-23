package ca.judacribz.gainzassist.firebase;

import android.app.Activity;
import java.util.ArrayList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.WorkoutHelper;

public class Database {

    // Constants
    // --------------------------------------------------------------------------------------------
    public final static String DEFAULT_WORKOUTS = "default_workouts";

    private final static String EMAIL = "email";
    private final static String USERNAME = "username";
    private final static String WORKOUTS = "workouts";
    private final static String SETS = "sets";

    public static final String USER_PATH = "users/%s";
    public static final String USER_WORKOUTS_PATH = "users/%s/workouts";
    // --------------------------------------------------------------------------------------------


    /* Adds data in firebase under "default_workouts/" to "users/<uid>/workouts/" */
    public static void addDefaultsToFirebase(final Activity activity, final ArrayList<Workout> workouts) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final WorkoutHelper workoutHelper = new WorkoutHelper(activity);

        if (user != null) {
            final String email = user.getEmail();
            final String uid = user.getUid();
            final DatabaseReference userRef =
                    FirebaseDatabase.getInstance().getReference(String.format(USER_PATH, uid));

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // FIREBASE: add user data
                    userRef.child(EMAIL).setValue(email);
                    userRef.child(USERNAME);

                    // FIREBASE: add workouts under user/uid/workouts/
                    for (Workout workout : workouts) {
                        userRef.child(WORKOUTS)
                               .child(workout.getName())
                               .setValue(workout.toMap());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }


    /* Adds data under "default_workouts/" in firebase to local db. Used with reference String
     * equal to "default_workouts" to call addDefaultsToFirebase function */
    public static void setFirebaseWorkouts(final Activity activity, final String reference) {

        // FIREBASE: Get reference to default_workouts
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(reference);

        // Listener for default_workouts object in firebase
        myRef.addValueEventListener(new ValueEventListener() {

            // Saves data to file
            @Override
            public void onDataChange(DataSnapshot workoutsShot) {
                if (workoutsShot != null) {

                    WorkoutHelper workoutHelper = new WorkoutHelper(activity);
                    ArrayList<Exercise> exercises;
                    Exercise exercise;
                    ArrayList<Set> sets;
                    Set set;
                    String workoutName;
                    ArrayList<Workout> workouts = new ArrayList<>();


                    // get each workout in default_workouts in firebase
                    for (DataSnapshot workoutShot : workoutsShot.getChildren()) {
                        workoutName = workoutShot.getKey();
                        exercises = new ArrayList<>();

                        // Get exercises from each workout in firebase
                        for (DataSnapshot exerciseShot : workoutShot.getChildren()) {

                            // Add sets
                            sets = new ArrayList<>();
                            for (DataSnapshot setShot : exerciseShot.child(SETS).getChildren()) {
                                set = setShot.getValue(Set.class);
                                if (set != null) {
                                    set.setSetNumber(Integer.valueOf(setShot.getKey()));
                                    sets.add(set);
                                }
                            }

                            // Adds sets to exercise, and add exercises list
                            exercise = exerciseShot.getValue(Exercise.class);
                            if (exercise != null) {
                                exercise.setName(exerciseShot.getKey());
                                exercise.setSets(sets);
                                exercises.add(exercise);
                            }
                        }

                        // Add workout name and exercises list to workouts list
                        workouts.add(new Workout(workoutName, exercises));
                    }

                    // Add workouts to WorkoutHelper db
                    workoutHelper.addWorkouts(workouts);

                    // FIREBASE: Add default workouts and user data
                    if (reference.equals(DEFAULT_WORKOUTS)) {
                        addDefaultsToFirebase(activity, workouts);
                    }

                    workoutHelper.close();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}