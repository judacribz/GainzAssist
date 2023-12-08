package ca.judacribz.gainzassist.models.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ca.judacribz.gainzassist.models.ExerciseSet;

@Dao
public interface SetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
