package ca.judacribz.gainzassist.models.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Session;

import java.util.List;

@Dao
public interface SetDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insert(ExerciseSet exerciseSet);

    @Query("SELECT * from  exercise_sets")
    LiveData<List<ExerciseSet>> getAll();

    @Query("SELECT * FROM exercise_sets WHERE exercise_id = :exerciseId")
    LiveData<List<ExerciseSet>> getLiveFromExercise(long exerciseId);

    @Query("SELECT * FROM exercise_sets WHERE exercise_id = :exerciseId")
    List<ExerciseSet> getFromExercise(long exerciseId);

    @Query("SELECT * from exercise_sets WHERE id = :id")
    ExerciseSet getId(long id);


    @Update
    void update(ExerciseSet... exerciseSets);


    @Delete
    void delete(ExerciseSet... exerciseSets);
}
