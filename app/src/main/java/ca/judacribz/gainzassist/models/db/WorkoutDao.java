package ca.judacribz.gainzassist.models.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ca.judacribz.gainzassist.models.Workout;

@Dao
public interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(Workout workout);


    @Query("SELECT * from workouts ORDER BY id")
    LiveData<List<Workout>> getAll();

    @Query("SELECT * from  workouts WHERE id = :id")
    LiveData<Workout> get(long id);

    @Query("SELECT * from  workouts WHERE name = :name")
    LiveData<Workout> getLiveFromName(String name);

    @Query("SELECT * from  workouts WHERE name = :name")
    Workout getFromName(String name);

    @Query("SELECT id from  workouts WHERE name = :name")
    Long getId(String name);

    @Query("SELECT id from  workouts WHERE id = :id")
    LiveData<Long> exists(long id);


    @Update
    void update(Workout... workout);


    @Query("DELETE FROM workouts")
    void deleteAll();

    @Query("DELETE from workouts WHERE name = :name")
    void delete(String name);

    @Delete
    void delete(Workout workout);
}
