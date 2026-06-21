package ca.gainzassist.test.fakes

import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.ExerciseSet
import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWorkoutRepository : WorkoutRepository {
    private val workouts = MutableStateFlow<List<Workout>>(emptyList())
    private val exercises = MutableStateFlow<List<Exercise>>(emptyList())
    private val sets = MutableStateFlow<List<ExerciseSet>>(emptyList())
    val insertedSessions = mutableListOf<Session>()

    override fun observeWorkouts(): Flow<List<Workout>> = workouts

    override fun observeWorkout(id: Long): Flow<Workout?> = workouts.map { list ->
        list.find { it.id == id }
    }

    override fun observeExercisesForWorkout(workoutId: Long): Flow<List<Exercise>> = exercises.map { list ->
        list.filter { it.workoutId == workoutId }
    }

    override fun observeExercise(exerciseId: Long): Flow<Exercise?> = exercises.map { list ->
        list.find { it.id == exerciseId }
    }

    override fun observeSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>> = sets.map { list ->
        list.filter { it.exerciseId == exerciseId }
    }

    override fun observeUniqueExerciseNames(): Flow<List<String>> = exercises.map { list ->
        list.mapNotNull { it.name }.distinct()
    }

    override suspend fun getWorkoutByName(name: String): Workout? = workouts.value.find { it.name == name }

    override suspend fun getWorkoutWithExercisesByName(name: String): Workout? {
        val workout = getWorkoutByName(name) ?: return null
        val workoutExercises = exercises.value.filter { it.workoutId == workout.id }
        workout.exercises = ArrayList(workoutExercises)
        return workout
    }

    val insertedWorkoutSyncFlags = mutableListOf<Boolean>()

    override suspend fun insertWorkout(workout: Workout, syncToFirebase: Boolean) {
        val current = workouts.value.toMutableList()
        if (workout.id == -1L || workout.id == 0L) {
            workout.id = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        }
        current.add(workout)
        workouts.value = current
        insertedWorkoutSyncFlags.add(syncToFirebase)
    }

    override suspend fun updateWorkout(workout: Workout) {
        val current = workouts.value.toMutableList()
        val index = current.indexOfFirst { it.id == workout.id }
        if (index >= 0) {
            current[index] = workout
            workouts.value = current
        }
    }

    override suspend fun deleteWorkout(workout: Workout) {
        val current = workouts.value.toMutableList()
        current.removeIf { it.id == workout.id }
        workouts.value = current
    }

    override suspend fun deleteWorkoutByName(workoutName: String) {
        val current = workouts.value.toMutableList()
        current.removeIf { it.name == workoutName }
        workouts.value = current
    }

    override suspend fun deleteAllWorkouts() {
        workouts.value = emptyList()
    }

    override suspend fun insertExercise(exercise: Exercise) {
        val current = exercises.value.toMutableList()
        if (exercise.id == -1L || exercise.id == 0L) {
            exercise.id = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        }
        current.add(exercise)
        exercises.value = current
    }

    override suspend fun insertExercises(exercises: List<Exercise>) {
        exercises.forEach { insertExercise(it) }
    }

    override suspend fun updateExercise(exercise: Exercise) {
        val current = exercises.value.toMutableList()
        val index = current.indexOfFirst { it.id == exercise.id }
        if (index >= 0) {
            current[index] = exercise
            exercises.value = current
        }
    }

    override suspend fun deleteExercise(exercise: Exercise) {
        val current = exercises.value.toMutableList()
        current.removeIf { it.id == exercise.id }
        exercises.value = current
    }

    override suspend fun insertSet(exerciseSet: ExerciseSet) {
        val current = sets.value.toMutableList()
        if (exerciseSet.id == -1L || exerciseSet.id == 0L) {
            exerciseSet.id = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        }
        current.add(exerciseSet)
        sets.value = current
    }

    override suspend fun deleteSet(exerciseSet: ExerciseSet) {
        val current = sets.value.toMutableList()
        current.removeIf { it.id == exerciseSet.id }
        sets.value = current
    }

    val completedSessionSyncFlags = mutableListOf<Boolean>()

    override suspend fun insertCompletedSession(session: Session, syncToFirebase: Boolean) {
        insertedSessions.add(session)
        completedSessionSyncFlags.add(syncToFirebase)
    }
}
