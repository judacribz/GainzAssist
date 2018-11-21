package ca.judacribz.gainzassist.models.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;
import ca.judacribz.gainzassist.models.ExerciseSet;

import java.util.List;

@Dao
public interface SetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExerciseSet exerciseSet);

    @Query("SELECT * FROM exercise_sets WHERE exercise_id = :exerciseId")
    LiveData<List<ExerciseSet>> getLiveFromExercise(int exerciseId);

    @Query("SELECT * FROM exercise_sets WHERE exercise_id = :exerciseId")
    List<ExerciseSet> getFromExercise(int exerciseId);


    @Update
    void update(ExerciseSet... exerciseSets);


    @Delete
    void delete(ExerciseSet... exerciseSets);
}
