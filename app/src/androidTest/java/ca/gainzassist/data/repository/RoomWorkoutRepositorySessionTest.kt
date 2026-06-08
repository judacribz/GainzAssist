package ca.gainzassist.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.gainzassist.constants.ExerciseConst.BARBELL
import ca.gainzassist.core.coroutines.DispatcherProvider
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.Session
import ca.gainzassist.models.Workout
import ca.gainzassist.models.db.WorkoutDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomWorkoutRepositorySessionTest {

    private lateinit var db: WorkoutDatabase
    private lateinit var repository: RoomWorkoutRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val testDispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = Dispatchers.Unconfined
            override val io: CoroutineDispatcher = Dispatchers.Unconfined
            override val default: CoroutineDispatcher = Dispatchers.Unconfined
        }

        repository = RoomWorkoutRepository(db, testDispatcherProvider)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testProgressionUpdatesExerciseWeight_110() = runBlocking {
        val workout = Workout("Progression Workout", ArrayList())
        repository.insertWorkout(workout)
        val workoutId = workout.id

        val exercise = Exercise(1, "Deadlift", "Strength", BARBELL, 3, 10, 100f, Exercise.SetsType.MAIN_SET)
        exercise.workoutId = workoutId
        repository.insertExercise(exercise)
        val exId = exercise.id

        val session = Session(workout)
        val finishedSets = arrayListOf(
            ExerciseSet(exercise, 0, 10, 100f),
            ExerciseSet(exercise, 1, 10, 100f),
            ExerciseSet(exercise, 2, 10, 100f)
        )
        exercise.finSets = finishedSets
        session.addExercise(exercise)

        repository.insertCompletedSession(session, syncToFirebase = false)

        val updatedEx = assertNotNullValue(db.exerciseDao().get(exId))
        assertEquals(110f, updatedEx.weight, 0.1f)
        assertEquals(110f, session.avgWeights.get(exercise.exerciseNumber, -1f), 0.1f)
    }

    @Test
    fun testProgressionUpdatesExerciseWeight_130() = runBlocking {
        val workout = Workout("OverPerformance Workout", ArrayList())
        repository.insertWorkout(workout)
        val workoutId = workout.id

        val exercise = Exercise(1, "Overhead Press", "Strength", BARBELL, 3, 10, 100f, Exercise.SetsType.MAIN_SET)
        exercise.workoutId = workoutId
        repository.insertExercise(exercise)
        val exId = exercise.id

        val session = Session(workout)
        val finishedSets = arrayListOf(
            ExerciseSet(exercise, 0, 10, 120f),
            ExerciseSet(exercise, 1, 10, 120f),
            ExerciseSet(exercise, 2, 10, 120f)
        )
        exercise.finSets = finishedSets
        session.addExercise(exercise)

        repository.insertCompletedSession(session, syncToFirebase = false)

        val updatedEx = assertNotNullValue(db.exerciseDao().get(exId))
        assertEquals(130f, updatedEx.weight, 0.1f)
        assertEquals(130f, session.avgWeights.get(exercise.exerciseNumber, -1f), 0.1f)
    }

    private fun <T> assertNotNullValue(value: T?): T {
        org.junit.Assert.assertNotNull(value)
        return value ?: error("Expected non-null value")
    }
}
