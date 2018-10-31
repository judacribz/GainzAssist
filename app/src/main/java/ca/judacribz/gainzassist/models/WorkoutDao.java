package ca.judacribz.gainzassist.models;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert
    void insert(Workout workout);

    @Query("DELETE FROM workouts")
    void deleteAll();

    @Query("SELECT * from workouts")
    LiveData<List<Workout>> getAllWorkouts();
}
