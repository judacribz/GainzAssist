package ca.judacribz.gainzassist.models.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;
import ca.judacribz.gainzassist.models.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(Exercise exercise);

    @Query("SELECT * from exercises WHERE workout_id = :workoutId")
    LiveData<List<Exercise>> getLiveFromWorkout(long workoutId);

    @Query("SELECT * from exercises WHERE workout_id = :workoutId ORDER BY exercise_number")
    List<Exercise> getFromWorkout(long workoutId);

    @Query("SELECT * from exercises WHERE id = :id")
    LiveData<Exercise> getLiveExerciseFromWorkout(long id);

    @Query("SELECT * from  exercises WHERE id = :id")
    LiveData<Exercise> getLive(long id);

    @Query("SELECT * from  exercises WHERE id = :id")
    Exercise get(long id);

    @Query("SELECT DISTINCT name FROM exercises")
    LiveData<List<String>> getAllUniqueNames();

    @Query("SELECT id from  exercises WHERE name = :name AND workout_id = :workoutId")
    long getId(String name, long workoutId);

    @Query("UPDATE exercises SET weight = :weight WHERE id = :id")
    void updateWeight(float weight, long id);

    @Update
    void update(Exercise... exercise);


    @Delete
    void delete(Exercise... exercises);
}
