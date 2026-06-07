package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Workout
import ca.gainzassist.test.fakes.FakeWorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutUseCaseTest {

    private lateinit var repository: FakeWorkoutRepository
    private lateinit var addWorkoutUseCase: AddWorkoutUseCase
    private lateinit var getWorkoutByNameUseCase: GetWorkoutByNameUseCase
    private lateinit var getWorkoutWithExercisesByNameUseCase: GetWorkoutWithExercisesByNameUseCase
    private lateinit var observeWorkoutsUseCase: ObserveWorkoutsUseCase
    private lateinit var observeUniqueExerciseNamesUseCase: ObserveUniqueExerciseNamesUseCase
    private lateinit var saveWorkoutUseCase: SaveWorkoutUseCase
    private lateinit var updateWorkoutUseCase: UpdateWorkoutUseCase
    private lateinit var deleteWorkoutUseCase: DeleteWorkoutUseCase
    private lateinit var deleteAllWorkoutsUseCase: DeleteAllWorkoutsUseCase
    private lateinit var exerciseExistsUseCase: ExerciseExistsUseCase

    @Before
    fun setup() {
        repository = FakeWorkoutRepository()
        addWorkoutUseCase = AddWorkoutUseCase(repository)
        getWorkoutByNameUseCase = GetWorkoutByNameUseCase(repository)
        getWorkoutWithExercisesByNameUseCase = GetWorkoutWithExercisesByNameUseCase(repository)
        observeWorkoutsUseCase = ObserveWorkoutsUseCase(repository)
        observeUniqueExerciseNamesUseCase = ObserveUniqueExerciseNamesUseCase(repository)
        saveWorkoutUseCase = SaveWorkoutUseCase(repository)
        updateWorkoutUseCase = UpdateWorkoutUseCase(repository)
        deleteWorkoutUseCase = DeleteWorkoutUseCase(repository)
        deleteAllWorkoutsUseCase = DeleteAllWorkoutsUseCase(repository)
        exerciseExistsUseCase = ExerciseExistsUseCase()
    }

    private fun <T> assertNotNullValue(value: T?): T {
        assertNotNull(value)
        return value ?: error("Expected non-null value")
    }

    @Test
    fun `addWorkoutUseCase inserts workout into repository`() = runTest {
        val workout = Workout("Push Day", null)
        addWorkoutUseCase(workout)

        val retrieved = getWorkoutByNameUseCase("Push Day")
        assertNotNull(retrieved)
        assertEquals("Push Day", retrieved?.name)
    }

    @Test
    fun `saveWorkoutUseCase inserts new workout`() = runTest {
        val workout = Workout("Pull Day", null)
        saveWorkoutUseCase(workout, isUpdate = false)

        val retrieved = getWorkoutByNameUseCase("Pull Day")
        assertNotNull(retrieved)
        assertEquals("Pull Day", retrieved?.name)
    }

    @Test
    fun `saveWorkoutUseCase updates existing workout`() = runTest {
        val workout = Workout("Leg Day", null)
        addWorkoutUseCase(workout)
        
        val retrieved = assertNotNullValue(getWorkoutByNameUseCase("Leg Day"))
        retrieved.name = "Leg Day Updated"
        
        saveWorkoutUseCase(retrieved, isUpdate = true)

        assertNull(getWorkoutByNameUseCase("Leg Day"))
        assertNotNull(getWorkoutByNameUseCase("Leg Day Updated"))
    }

    @Test
    fun `updateWorkoutUseCase updates workout`() = runTest {
        val workout = Workout("Yoga", null)
        addWorkoutUseCase(workout)
        
        val retrieved = assertNotNullValue(getWorkoutByNameUseCase("Yoga"))
        retrieved.name = "Advanced Yoga"
        
        updateWorkoutUseCase(retrieved)

        assertEquals("Advanced Yoga", getWorkoutByNameUseCase("Advanced Yoga")?.name)
    }

    @Test
    fun `deleteWorkoutUseCase removes workout by name`() = runTest {
        val workout = Workout("Short Workout", null)
        addWorkoutUseCase(workout)
        
        deleteWorkoutUseCase("Short Workout")

        assertNull(getWorkoutByNameUseCase("Short Workout"))
    }

    @Test
    fun `deleteAllWorkoutsUseCase clears repository`() = runTest {
        addWorkoutUseCase(Workout("W1", null))
        addWorkoutUseCase(Workout("W2", null))
        
        assertEquals(2, observeWorkoutsUseCase().first().size)
        
        deleteAllWorkoutsUseCase()
        
        assertEquals(0, observeWorkoutsUseCase().first().size)
    }

    @Test
    fun `getWorkoutWithExercisesByNameUseCase retrieves workout and its exercises`() = runTest {
        val workout = Workout("Full Body", null)
        addWorkoutUseCase(workout)
        val savedWorkout = assertNotNullValue(getWorkoutByNameUseCase("Full Body"))
        
        val exercise = Exercise().apply {
            name = "Squat"
            workoutId = savedWorkout.id
        }
        repository.insertExercise(exercise)
        
        val retrieved = getWorkoutWithExercisesByNameUseCase("Full Body")
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.exercises?.size)
        assertEquals("Squat", retrieved?.exercises?.get(0)?.name)
    }

    @Test
    fun `observeUniqueExerciseNamesUseCase reflects changes`() = runTest {
        val workout = Workout("W1", null)
        addWorkoutUseCase(workout)
        val savedWorkout = assertNotNullValue(getWorkoutByNameUseCase("W1"))
        
        repository.insertExercise(Exercise().apply { name = "Pushup"; workoutId = savedWorkout.id })
        repository.insertExercise(Exercise().apply { name = "Pushup"; workoutId = savedWorkout.id })
        repository.insertExercise(Exercise().apply { name = "Pullup"; workoutId = savedWorkout.id })
        
        val names = observeUniqueExerciseNamesUseCase().first()
        assertEquals(2, names.size)
        assertTrue(names.contains("Pushup"))
        assertTrue(names.contains("Pullup"))
    }

    @Test
    fun `exerciseExistsUseCase validates names correctly`() {
        val existing = listOf("Bench Press", "Squat")
        
        assertTrue(exerciseExistsUseCase(existing, "Bench Press"))
        assertTrue(exerciseExistsUseCase(existing, "bench press "))
        assertTrue(exerciseExistsUseCase(existing, "SQUAT"))
        assertFalse(exerciseExistsUseCase(existing, "Deadlift"))
        assertFalse(exerciseExistsUseCase(existing, ""))
        assertFalse(exerciseExistsUseCase(existing, "   "))
    }
}
