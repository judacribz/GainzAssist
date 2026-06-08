package ca.gainzassist.models.db

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import ca.gainzassist.domain.usecase.workout.CalculateNextExerciseWeightUseCase
import ca.gainzassist.interfaces.OnWorkoutReceivedListener
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.Session
import ca.gainzassist.models.Workout
import ca.gainzassist.util.firebase.Database.addWorkoutSessionFirebase
import ca.gainzassist.util.firebase.Database.deleteWorkoutFirebase
import com.google.firebase.database.DataSnapshot

class WorkoutRepo(app: Application) {
    private var workoutShot: DataSnapshot? = null
    var onWorkoutReceivedListener: OnWorkoutReceivedListener? = null

    enum class TableTxn {
        WORKOUTS_TXN,
        EXERCISES_TXN,
        SESSIONS_TXN,
        SETS_TXN
    }

    enum class RepoTask {
        GET_WORKOUT,
        GET_EXERCISES,
        INSERT_EXERCISE,
        INSERT_WORKOUT,
        INSERT_SESSION,
        INSERT_SET,
        UPDATE_WORKOUT,
        UPDATE_EXERCISE,
        UPDATE_EXERCISE_WEIGHT,
        UPDATE_SET,
        DELETE_ALL_WORKOUTS,
        DELETE_WORKOUT,
        DELETE_EXERCISE,
        DELETE_SET
    }

    init {
        val db = WorkoutDatabase.getDatabase(app)
        workoutDao = db.workoutDao()
        exerciseDao = db.exerciseDao()
        setDao = db.setDao()
        sessionDao = db.sessionDao()
    }

    // CRUD functions
    // ============================================================================================
    // CREATE
    // --------------------------------------------------------------------------------------------
    fun insertWorkout(workout: Workout) {
        Thread {
            workoutDao.insert(workout)
            insertExercise(*workout.exercises.toTypedArray())
        }.start()
    }

    fun insertExercise(vararg exercises: Exercise) {
        for (ex in exercises) {
            exerciseDao.insert(ex)
        }
    }

    fun insertSession(session: Session, toFireBase: Boolean) {
        Thread {
            sessionDao.insert(session)
            val calculateNextWeight = CalculateNextExerciseWeightUseCase()
            for (ex in session.sessionExs) {
                insertSet(*ex.getFinishedSetsList().toTypedArray())
                if (ex.setsType == Exercise.SetsType.MAIN_SET) {
                    val nextWeight = calculateNextWeight.invoke(ex, ex.getFinishedSetsList())
                    exerciseDao.updateWeight(nextWeight, ex.id)
                }
            }
        }.start()

        if (toFireBase) {
            addWorkoutSessionFirebase(session)
        }
    }

    // --------------------------------------------------------------------------------------------

    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    fun getAllWorkoutsLive(): LiveData<List<Workout>> {
        return workoutDao.getAll()
    }

    fun getAllSessionsLive(): LiveData<List<Session>> {
        return sessionDao.getAll()
    }

    fun getAllSetsLive(): LiveData<List<ExerciseSet>> {
        return setDao.getAll()
    }

    fun getWorkout(id: Long): LiveData<Workout> {
        return workoutDao.get(id)
    }

    fun getWorkoutFromName(context: Context, name: String) {
        Thread {
            onWorkoutReceivedListener = context as OnWorkoutReceivedListener
            val workout = workoutDao.getFromName(name) ?: return@Thread

            for (exercise in exerciseDao.getFromWorkout(workout.id)) {
                workout.addExercise(exercise)
            }

            onWorkoutReceivedListener?.onWorkoutsReceived(workout)
            onWorkoutReceivedListener = null
        }.start()
    }

    fun getExercisesFromWorkout(workoutId: Long): LiveData<List<Exercise>> {
        return exerciseDao.getLiveFromWorkout(workoutId)
    }

    fun getExercise(id: Long): LiveData<Exercise> {
        return exerciseDao.getLive(id)
    }

    fun getAllUniqueExerciseNames(): LiveData<List<String>> {
        return exerciseDao.getAllUniqueNames()
    }

    fun getSetsFromExercise(exerciseId: Long): LiveData<List<ExerciseSet>> {
        return setDao.getLiveFromExercise(exerciseId)
    }
    // --------------------------------------------------------------------------------------------

    // UPDATE
    // --------------------------------------------------------------------------------------------
    fun updateWorkout(workout: Workout) {
        Thread { workoutDao.update(workout) }.start()
        updateExercise(*workout.exercises.toTypedArray())
    }

    fun updateExercise(vararg exercises: Exercise) {
        for (ex in exercises) {
            Thread {
                if (exerciseDao.get(ex.id) == null) {
                    insertExercise(ex)
                } else {
                    exerciseDao.update(ex)
                }
            }.start()
        }
    }
    // --------------------------------------------------------------------------------------------

    // DELETE
    // --------------------------------------------------------------------------------------------
    fun deleteAllWorkouts() {
        Thread { workoutDao.deleteAll() }.start()
    }

    fun deleteWorkout(workout: Workout) {
        Thread { workoutDao.delete(workout) }.start()
        deleteWorkoutFirebase(workout.name)
    }

    fun deleteWorkout(workoutName: String) {
        Thread { workoutDao.delete(workoutName) }.start()
        deleteWorkoutFirebase(workoutName)
    }

    fun deleteExercise(vararg exercise: Exercise) {
        Thread { exerciseDao.delete(*exercise) }.start()
    }

    fun deleteSet(exerciseSet: ExerciseSet) {
        Thread { setDao.delete(exerciseSet) }.start()
    }
    // --------------------------------------------------------------------------------------------
    // ============================================================================================

    companion object {
        private lateinit var workoutDao: WorkoutDao
        private lateinit var exerciseDao: ExerciseDao
        private lateinit var setDao: SetDao
        private lateinit var sessionDao: SessionDao

        @JvmStatic
        fun insertSet(vararg exerciseSets: ExerciseSet) {
            for (set in exerciseSets) {
                Thread { setDao.insert(set) }.start()
            }
        }
    }
}
