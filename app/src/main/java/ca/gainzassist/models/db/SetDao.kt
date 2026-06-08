package ca.gainzassist.models.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ca.gainzassist.models.ExerciseSet

@Dao
interface SetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exerciseSet: ExerciseSet)

    @Query("SELECT * from  exercise_sets")
    fun getAll(): LiveData<List<ExerciseSet>>

    @Query("SELECT * FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getLiveFromExercise(exerciseId: Long): LiveData<List<ExerciseSet>>

    @Query("SELECT * FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getFromExercise(exerciseId: Long): List<ExerciseSet>

    @Query("SELECT * from exercise_sets WHERE id = :id")
    fun getId(id: Long): ExerciseSet?

    @Update
    fun update(vararg exerciseSets: ExerciseSet)

    @Delete
    fun delete(vararg exerciseSets: ExerciseSet)
}
