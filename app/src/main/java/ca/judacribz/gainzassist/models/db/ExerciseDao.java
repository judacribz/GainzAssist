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
    LiveData<List<Exercise>> getLiveFromWorkout(int workoutId);

    @Query("SELECT * from exercises WHERE workout_id = :workoutId")
    List<Exercise> getFromWorkout(int workoutId);

    @Query("SELECT * from  exercises WHERE id = :id")
    LiveData<Exercise> get(long id);

    @Query("SELECT DISTINCT name FROM exercises")
    LiveData<List<String>> getAllUniqueNames();

    @Query("SELECT id from  exercises WHERE name = :name AND workout_id = :workoutId")
    int getId(String name, int workoutId);


    @Query("UPDATE exercises SET weight = :weight WHERE id = :id")
    void updateWeight(float weight, int id);

    @Update
    void update(Exercise... exercise);


    @Delete
    void delete(Exercise... exercises);
}
