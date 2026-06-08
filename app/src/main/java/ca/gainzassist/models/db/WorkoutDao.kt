package ca.gainzassist.models.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ca.gainzassist.models.Workout

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(workout: Workout): Long

    @Query("SELECT * from workouts ORDER BY id")
    fun getAll(): LiveData<List<Workout>>

    @Query("SELECT * from  workouts WHERE id = :id")
    fun get(id: Long): LiveData<Workout>

    @Query("SELECT * from  workouts WHERE name = :name")
    fun getLiveFromName(name: String): LiveData<Workout>

    @Query("SELECT * from  workouts WHERE name = :name")
    fun getFromName(name: String): Workout?

    @Query("SELECT id from  workouts WHERE name = :name")
    fun getId(name: String): Long?

    @Query("SELECT id from  workouts WHERE id = :id")
    fun exists(id: Long): LiveData<Long>

    @Update
    fun update(vararg workout: Workout)

    @Query("DELETE FROM workouts")
    fun deleteAll()

    @Query("DELETE from workouts WHERE name = :name")
    fun delete(name: String)

    @Delete
    fun delete(workout: Workout)
}
