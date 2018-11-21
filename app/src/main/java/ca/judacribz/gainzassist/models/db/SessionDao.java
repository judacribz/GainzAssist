package ca.judacribz.gainzassist.models.db;

import android.arch.persistence.room.*;
import ca.judacribz.gainzassist.models.Session;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(Session session);


    @Query("SELECT id FROM  sessions WHERE timestamp = :timestamp")
    int getId(long timestamp);

//    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId")
//    LiveData<List<ExerciseSet>> getLiveFromExercise(int exerciseId);
//
//    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId")
//    List<ExerciseSet> getFromExercise(int exerciseId);


    @Update
    void update(Session... sessions);


    @Delete
    void delete(Session... sessions);


}
