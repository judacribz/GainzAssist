package ca.judacribz.gainzassist.models.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;
import ca.judacribz.gainzassist.models.Set;

import java.util.List;

@Dao
public interface SetDao {

    @Insert
    void insert(Set set);

    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId")
    LiveData<List<Set>> getLiveFromExercise(long exerciseId);

    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId")
    List<Set> getFromExercise(long exerciseId);


    @Update
    void update(Set... sets);


    @Delete
    void delete(Set... sets);
}
