package ca.judacribz.gainzassist.models;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(Workout workout);


    @Query("SELECT * from  workouts")
    LiveData<List<Workout>> getAll();

    @Query("SELECT * from  workouts WHERE id = :id")
    LiveData<Workout> get(long id);

    @Query("SELECT * from  workouts WHERE name = :name")
    LiveData<Workout> getFromName(String name);



    @Update
    void update(Workout... workout);


    @Delete
    void delete(Workout... workout);
}
