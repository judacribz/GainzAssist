package ca.judacribz.gainzassist.models;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert
    Long insert(Exercise exercise);


    @Query("SELECT * from exercises WHERE workout_id = :workoutId")
    LiveData<List<Exercise>> getLiveFromWorkout(long workoutId);

    @Query("SELECT * from exercises WHERE workout_id = :workoutId")
    List<Exercise> getFromWorkout(long workoutId);

    @Query("SELECT * from  exercises WHERE id = :id")
    LiveData<Exercise> get(long id);

    @Query("SELECT DISTINCT name FROM exercises")
    LiveData<List<String>> getAllUniqueNames();


    @Update
    void update(Exercise... exercise);


    @Delete
    void delete(Exercise... exercises);
}
