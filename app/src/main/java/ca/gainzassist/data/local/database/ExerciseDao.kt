package ca.gainzassist.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ca.gainzassist.domain.model.Exercise

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exercise: Exercise): Long

    @Query("SELECT * from exercises WHERE workout_id = :workoutId")
    fun getLiveFromWorkout(workoutId: Long): LiveData<List<Exercise>>

    @Query("SELECT * from exercises WHERE workout_id = :workoutId ORDER BY exercise_number")
    fun getFromWorkout(workoutId: Long): List<Exercise>

    @Query("SELECT * from exercises WHERE id = :id")
    fun getLiveExerciseFromWorkout(id: Long): LiveData<Exercise>

    @Query("SELECT * from  exercises WHERE id = :id")
    fun getLive(id: Long): LiveData<Exercise>

    @Query("SELECT * from  exercises WHERE id = :id")
    fun getName(id: Long): Exercise?

    @Query("SELECT DISTINCT name FROM exercises")
    fun getAllUniqueNames(): LiveData<List<String>>

    @Query("SELECT id from  exercises WHERE name = :name AND workout_id = :workoutId")
    fun getId(name: String, workoutId: Long): Long

    @Query("UPDATE exercises SET weight = :weight WHERE id = :id")
    fun updateWeight(weight: Float, id: Long)

    @Update
    fun update(vararg exercise: Exercise)

    @Delete
    fun delete(vararg exercises: Exercise)
}
