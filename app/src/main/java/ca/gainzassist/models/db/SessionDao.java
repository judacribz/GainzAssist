package ca.gainzassist.models.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;
import ca.gainzassist.models.Session;
import java.util.List;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(Session session);


    @Query("SELECT * from  sessions")
    LiveData<List<Session>> getAll();
//    @Query("SELECT id FROM  sessions WHERE timestamp = :timestamp")
//    int getId(long timestamp);

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
