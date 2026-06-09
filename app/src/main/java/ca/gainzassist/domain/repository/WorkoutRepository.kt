package ca.gainzassist.domain.repository

import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.Session
import ca.gainzassist.models.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeWorkouts(): Flow<List<Workout>>
    fun observeWorkout(id: Long): Flow<Workout?>
    fun observeExercisesForWorkout(workoutId: Long): Flow<List<Exercise>>
    fun observeExercise(exerciseId: Long): Flow<Exercise?>
    fun observeSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>>
    fun observeUniqueExerciseNames(): Flow<List<String>>

    suspend fun getWorkoutByName(name: String): Workout?
    suspend fun getWorkoutWithExercisesByName(name: String): Workout?

    suspend fun insertWorkout(workout: Workout, syncToFirebase: Boolean = true)
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)
    suspend fun deleteWorkoutByName(workoutName: String)
    suspend fun deleteAllWorkouts()

    suspend fun insertExercise(exercise: Exercise)
    suspend fun insertExercises(exercises: List<Exercise>)
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(exercise: Exercise)

    suspend fun insertSet(exerciseSet: ExerciseSet)
    suspend fun deleteSet(exerciseSet: ExerciseSet)

    suspend fun insertCompletedSession(session: Session, syncToFirebase: Boolean = true)
}
