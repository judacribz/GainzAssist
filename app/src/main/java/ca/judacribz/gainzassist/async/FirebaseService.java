package ca.judacribz.gainzassist.async;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.WorkoutHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.firebase.Database.getWorkoutsRef;


public class FirebaseService extends IntentService implements ChildEventListener {

    private final static String SETS = "sets";

    WorkoutHelper workoutHelper;
    ArrayList<String> workoutNames;
    ArrayList<Exercise> exercises;
    ArrayList<Set> sets;

    Exercise exercise;
    Set set;
    String workoutName;

    public FirebaseService() {
        super("FirebaseService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        workoutHelper = new WorkoutHelper(getApplicationContext());
        workoutNames = workoutHelper.getAllWorkoutNames();
        getWorkoutsRef().addChildEventListener(this);

        return Service.START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public void onChildAdded(DataSnapshot workoutShot, String s) {

        workoutName = workoutShot.getKey();
        if (!workoutNames.contains(workoutName)) {

            workoutHelper.addWorkout(extractWorkout(workoutShot));
        }
    }

    @Override
    public void onChildChanged(DataSnapshot workoutShot, String s) {

        workoutHelper.updateWorkout(workoutShot.getKey(), extractWorkout(workoutShot));

    }

    @Override
    public void onChildRemoved(DataSnapshot workoutShot) {
        workoutHelper.deleteWorkout(workoutShot.getKey());

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public Workout extractWorkout(DataSnapshot workoutShot) {
        exercises = new ArrayList<>();
        for (DataSnapshot exerciseShot : workoutShot.getChildren()) {
            // Add set to sets list
            sets = new ArrayList<>();
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
        }

        return new Workout(workoutShot.getKey(), exercises);
    }
}
