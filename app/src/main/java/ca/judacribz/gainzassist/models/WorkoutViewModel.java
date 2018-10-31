package ca.judacribz.gainzassist.models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {
    private WorkoutRepo workoutRepo;
    private LiveData<List<Workout>> workouts;

    public WorkoutViewModel(Application app) {
        super(app);
        workoutRepo = new WorkoutRepo(app);
        workouts = workoutRepo.getAllWorkouts();
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return workouts;
    }

    public void insert(Workout workout) {
        workoutRepo.insert(workout);
    }

    public void deleteAll() {
        workoutRepo.deleteAll();
    }

}
