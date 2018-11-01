package ca.judacribz.gainzassist.models;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface SetDao {

    @Insert
    void insert(Set set);


    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId")
    LiveData<List<Set>> getSetsFromExercise(int exerciseId);


    @Update
    void update(Set... sets);


    @Delete
    void delete(Set... sets);
}
