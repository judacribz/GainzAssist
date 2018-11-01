package ca.judacribz.gainzassist.models;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert
    void insert(Exercise exercise);


    @Query("SELECT * from exercises WHERE workout_id = :workoutId")
    LiveData<List<Exercise>> getFromWorkout(int workoutId);

    @Query("SELECT * from  exercises WHERE id = :id")
    LiveData<Exercise> get(int id);

    @Query("SELECT DISTINCT name FROM exercises")
    LiveData<List<String>> getAllUniqueNames();


    @Update
    void update(Exercise... exercise);


    @Delete
    void delete(Exercise... exercises);
}
