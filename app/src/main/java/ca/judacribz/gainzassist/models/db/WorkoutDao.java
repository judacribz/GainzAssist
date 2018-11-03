package ca.judacribz.gainzassist.models.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;
import ca.judacribz.gainzassist.models.Workout;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(Workout workout);


    @Query("SELECT * from  workouts")
    LiveData<List<Workout>> getAll();

    @Query("SELECT * from  workouts WHERE id = :id")
    LiveData<Workout> get(int id);

    @Query("SELECT * from  workouts WHERE name = :name")
    LiveData<Workout> getLiveFromName(String name);

    @Query("SELECT * from  workouts WHERE name = :name")
    Workout getFromName(String name);

    @Query("SELECT id from  workouts WHERE name = :name")
    int getId(String name);


    @Update
    void update(Workout... workout);

    @Query("DELETE from workouts")
    void deleteAll();

    @Query("DELETE from workouts WHERE name = :name")
    void delete(String name);
}
