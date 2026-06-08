package ca.gainzassist.data.repository

import androidx.lifecycle.asFlow
import ca.gainzassist.core.coroutines.DispatcherProvider
import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.Session
import ca.gainzassist.models.Workout
import ca.gainzassist.models.db.WorkoutDatabase
import ca.gainzassist.util.firebase.Database
import ca.gainzassist.domain.usecase.workout.CalculateNextExerciseWeightUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomWorkoutRepository(
    database: WorkoutDatabase,
    private val dispatcherProvider: DispatcherProvider
) : WorkoutRepository {

    private val workoutDao = database.workoutDao()
    private val exerciseDao = database.exerciseDao()
    private val setDao = database.setDao()
    private val sessionDao = database.sessionDao()

    override fun observeWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAll().asFlow()
    }

    override fun observeWorkout(id: Long): Flow<Workout?> {
        return workoutDao.get(id).asFlow()
    }

    override fun observeExercisesForWorkout(workoutId: Long): Flow<List<Exercise>> {
        return exerciseDao.getLiveFromWorkout(workoutId).asFlow()
    }

    override fun observeExercise(exerciseId: Long): Flow<Exercise?> {
        return exerciseDao.getLive(exerciseId).asFlow()
    }

    override fun observeSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>> {
        return setDao.getLiveFromExercise(exerciseId).asFlow()
    }

    override fun observeUniqueExerciseNames(): Flow<List<String>> {
        return exerciseDao.getAllUniqueNames().asFlow()
    }

    override suspend fun getWorkoutByName(name: String): Workout? = withContext(dispatcherProvider.io) {
        workoutDao.getFromName(name)
    }

    override suspend fun getWorkoutWithExercisesByName(name: String): Workout? = withContext(dispatcherProvider.io) {
        val workout = workoutDao.getFromName(name) ?: return@withContext null
        val exercises = exerciseDao.getFromWorkout(workout.id)
        for (exercise in exercises) {
            workout.addExercise(exercise)
        }
        workout
    }

    override suspend fun insertWorkout(workout: Workout) = withContext(dispatcherProvider.io) {
        val newId = workoutDao.insert(workout)
        workout.id = newId
        
        val exercises = workout.exercises
        if (exercises.isNotEmpty()) {
            for (ex in exercises) {
                ex.workoutId = newId
            }
            insertExercises(exercises)
        }
        Database.addWorkoutFirebase(workout)
    }

    override suspend fun updateWorkout(workout: Workout) = withContext(dispatcherProvider.io) {
        workoutDao.update(workout)
        
        val exercises = workout.exercises
        if (exercises.isNotEmpty()) {
            for (ex in exercises) {
                ex.workoutId = workout.id
                if (exerciseDao.get(ex.id) == null) {
                    exerciseDao.insert(ex)
                } else {
                    exerciseDao.update(ex)
                }
            }
        }
        Database.addWorkoutFirebase(workout)
    }

    override suspend fun deleteWorkout(workout: Workout) = withContext(dispatcherProvider.io) {
        workoutDao.delete(workout)
        Database.deleteWorkoutFirebase(workout.name)
    }

    override suspend fun deleteWorkoutByName(workoutName: String) = withContext(dispatcherProvider.io) {
        workoutDao.delete(workoutName)
        Database.deleteWorkoutFirebase(workoutName)
    }

    override suspend fun deleteAllWorkouts() = withContext(dispatcherProvider.io) {
        workoutDao.deleteAll()
    }

    override suspend fun insertExercise(exercise: Exercise) = withContext<Unit>(dispatcherProvider.io) {
        exerciseDao.insert(exercise)
    }

    override suspend fun insertExercises(exercises: List<Exercise>) = withContext(dispatcherProvider.io) {
        for (ex in exercises) {
            exerciseDao.insert(ex)
        }
    }

    override suspend fun updateExercise(exercise: Exercise) = withContext(dispatcherProvider.io) {
        exerciseDao.update(exercise)
    }

    override suspend fun deleteExercise(exercise: Exercise) = withContext(dispatcherProvider.io) {
        exerciseDao.delete(exercise)
    }

    override suspend fun insertSet(exerciseSet: ExerciseSet) = withContext(dispatcherProvider.io) {
        setDao.insert(exerciseSet)
    }

    override suspend fun deleteSet(exerciseSet: ExerciseSet) = withContext(dispatcherProvider.io) {
        setDao.delete(exerciseSet)
    }

    override suspend fun insertCompletedSession(session: Session, syncToFirebase: Boolean) = withContext(dispatcherProvider.io) {
        sessionDao.insert(session)
        val calculateNextWeight = CalculateNextExerciseWeightUseCase()

        for (exercise in session.sessionExs) {
            val finishedSets = exercise.getFinishedSetsList()
            for (set in finishedSets) {
                setDao.insert(set)
            }
            if (exercise.setsType == Exercise.SetsType.MAIN_SET) {
                val nextWeight = calculateNextWeight(exercise, finishedSets)
                exerciseDao.updateWeight(nextWeight, exercise.id)
            }
        }

        if (syncToFirebase) {
            Database.addWorkoutSessionFirebase(session)
        }
    }
}
