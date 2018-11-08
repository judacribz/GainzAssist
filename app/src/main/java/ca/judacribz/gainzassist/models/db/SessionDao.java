package ca.judacribz.gainzassist.models.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Set;

import java.util.List;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Session session);

//    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId")
//    LiveData<List<Set>> getLiveFromExercise(int exerciseId);
//
//    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId")
//    List<Set> getFromExercise(int exerciseId);


    @Update
    void update(Session... sessions);


    @Delete
    void delete(Session... sessions);
}
