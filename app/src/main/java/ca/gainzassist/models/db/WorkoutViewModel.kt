package ca.gainzassist.models.db

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.Session
import ca.gainzassist.models.Workout

class WorkoutViewModel(app: Application) : AndroidViewModel(app) {
    private val workoutRepo = WorkoutRepo(app)

    // CREATE
    // --------------------------------------------------------------------------------------------
    fun insertWorkout(vararg workouts: Workout) {
        for (workout in workouts) {
            workoutRepo.insertWorkout(workout)
        }
    }

    fun insertExercise(vararg exercises: Exercise) {
        for (exercise in exercises) {
            workoutRepo.insertExercise(exercise)
        }
    }

    fun insertSession(vararg sessions: Session) {
        for (session in sessions) {
            workoutRepo.insertSession(session, true)
        }
    }

    fun insertSet(vararg exerciseSets: ExerciseSet) {
        for (exerciseSet in exerciseSets) {
            WorkoutRepo.insertSet(exerciseSet)
        }
    }
    // --------------------------------------------------------------------------------------------

    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    val allWorkouts: LiveData<List<Workout>>
        get() = workoutRepo.getAllWorkoutsLive()

    val allSessions: LiveData<List<Session>>
        get() = workoutRepo.getAllSessionsLive()

    val allSets: LiveData<List<ExerciseSet>>
        get() = workoutRepo.getAllSetsLive()

    fun getWorkout(id: Int): LiveData<Workout> {
        return workoutRepo.getWorkout(id.toLong())
    }

    fun getWorkoutFromName(context: Context?, name: String) {
        workoutRepo.getWorkoutFromName(context, name)
    }

    fun getExercisesFromWorkout(workoutId: Int): LiveData<List<Exercise>> {
        return workoutRepo.getExercisesFromWorkout(workoutId.toLong())
    }

    fun getExercise(id: Long): LiveData<Exercise> {
        return workoutRepo.getExercise(id)
    }

    val allUniqueExerciseNames: LiveData<List<String>>
        get() = workoutRepo.getAllUniqueExerciseNames()

    fun getSetsFromExercise(exerciseId: Long): LiveData<List<ExerciseSet>> {
        return workoutRepo.getSetsFromExercise(exerciseId)
    }
    // --------------------------------------------------------------------------------------------

    // UPDATE
    // --------------------------------------------------------------------------------------------
    fun updateWorkout(workout: Workout) {
        workoutRepo.updateWorkout(workout)
    }

    internal fun updateExercise(exercise: Exercise) {
        workoutRepo.updateExercise(exercise)
    }
    // --------------------------------------------------------------------------------------------

    // DELETE
    // --------------------------------------------------------------------------------------------
    fun deleteAllWorkouts() {
        workoutRepo.deleteAllWorkouts()
    }

    fun deleteWorkout(workout: Workout) {
        workoutRepo.deleteWorkout(workout)
    }

    fun deleteWorkout(workoutName: String) {
        workoutRepo.deleteWorkout(workoutName)
    }

    fun deleteExercise(exercise: Exercise) {
        workoutRepo.deleteExercise(exercise)
    }

    fun deleteSet(exerciseSet: ExerciseSet) {
        workoutRepo.deleteSet(exerciseSet)
    }
    // --------------------------------------------------------------------------------------------
}
