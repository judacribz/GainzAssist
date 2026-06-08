package ca.gainzassist.models.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.gainzassist.constants.ExerciseConst.BARBELL
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.Session
import ca.gainzassist.models.Workout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDatabaseCharacterizationTest {

    private lateinit var db: WorkoutDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var sessionDao: SessionDao
    private lateinit var setDao: SetDao
    private lateinit var workoutRepo: WorkoutRepo

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Inject the in-memory database into the singleton so WorkoutRepo uses it
        val instanceField = WorkoutDatabase::class.java.getDeclaredField("INSTANCE")
        instanceField.isAccessible = true
        instanceField.set(null, db)

        workoutDao = db.workoutDao()
        exerciseDao = db.exerciseDao()
        sessionDao = db.sessionDao()
        setDao = db.setDao()

        workoutRepo = WorkoutRepo(context as android.app.Application)
    }

    @After
    fun closeDb() {
        db.close()
        val instanceField = WorkoutDatabase::class.java.getDeclaredField("INSTANCE")
        instanceField.isAccessible = true
        instanceField.set(null, null)
    }

    @Test
    fun testRoomDaoBehavior() {
        // Insert workout
        val workout = Workout("Test Workout", ArrayList())
        val workoutId = workoutDao.insert(workout)
        assertTrue(workoutId > 0)

        // Insert exercises for workout
        val exercise1 = Exercise(1, "Bench Press", "Strength", BARBELL, 3, 10, 100f, Exercise.SetsType.MAIN_SET)
        exercise1.workoutId = workoutId
        val ex1Id = exerciseDao.insert(exercise1)
        exercise1.id = ex1Id

        val exercise2 = Exercise(2, "Squat", "Strength", BARBELL, 3, 10, 200f, Exercise.SetsType.MAIN_SET)
        exercise2.workoutId = workoutId
        val ex2Id = exerciseDao.insert(exercise2)
        exercise2.id = ex2Id

        // Query workout by name
        val queriedWorkout = workoutDao.getFromName("Test Workout")
        assertNotNull(queriedWorkout)
        assertEquals(workoutId, queriedWorkout.id)

        // Query exercises by workout id ordered by exercise_number
        val exercises = exerciseDao.getFromWorkout(workoutId)
        assertEquals(2, exercises.size)
        assertEquals("Bench Press", exercises[0].name)
        assertEquals("Squat", exercises[1].name)

        // Update exercise weight through ExerciseDao.updateWeight
        exerciseDao.updateWeight(110f, ex1Id)
        val updatedEx1 = exerciseDao.get(ex1Id)
        assertEquals(110f, updatedEx1.weight, 0.1f)

        // Insert session
        val session = Session(queriedWorkout)
        val sessionId = sessionDao.insert(session)
        assertTrue(sessionId > 0)

        // Insert exercise sets
        val set = ExerciseSet(exercise1, 0, 10, 110f)
        setDao.insert(set)
        val sets = setDao.getFromExercise(ex1Id)
        assertEquals(1, sets.size)
        assertEquals(110f, sets[0].weight, 0.1f)
    }

    @Test
    fun testProgressionPersistenceBehaviorExactTarget() {
        val workout = Workout("Progression Workout", ArrayList())
        val workoutId = workoutDao.insert(workout)

        // Create workout with exercise target 100 lb x 10 reps x 3 sets
        val exercise = Exercise(1, "Deadlift", "Strength", BARBELL, 3, 10, 100f, Exercise.SetsType.MAIN_SET)
        exercise.workoutId = workoutId
        val exId = exerciseDao.insert(exercise)
        exercise.id = exId

        // Create completed session with 3 finished sets at 100 x 10
        val session = Session(workout)
        val finishedSets = arrayListOf(
            ExerciseSet(exercise, 0, 10, 100f),
            ExerciseSet(exercise, 1, 10, 100f),
            ExerciseSet(exercise, 2, 10, 100f)
        )
        exercise.finSets = finishedSets
        session.addExercise(exercise)

        // Insert session through the current legacy path
        workoutRepo.insertSession(session, false)

        // Wait for the background thread to finish its work
        waitForCondition {
            exerciseDao.get(exId).weight == 110f
        }

        val updatedEx = exerciseDao.get(exId)
        assertEquals(110f, updatedEx.weight, 0.1f)
    }

    @Test
    fun testProgressionPersistenceBehaviorOverPerformance() {
        val workout = Workout("OverPerformance Workout", ArrayList())
        val workoutId = workoutDao.insert(workout)

        // Create workout with exercise target 100 lb x 10 reps x 3 sets
        val exercise = Exercise(1, "Overhead Press", "Strength", BARBELL, 3, 10, 100f, Exercise.SetsType.MAIN_SET)
        exercise.workoutId = workoutId
        val exId = exerciseDao.insert(exercise)
        exercise.id = exId

        // 120 x 10 should update next target to 130
        val session = Session(workout)
        val finishedSets = arrayListOf(
            ExerciseSet(exercise, 0, 10, 120f),
            ExerciseSet(exercise, 1, 10, 120f),
            ExerciseSet(exercise, 2, 10, 120f)
        )
        exercise.finSets = finishedSets
        session.addExercise(exercise)

        // Insert session through the current legacy path
        workoutRepo.insertSession(session, false)

        // Wait for the background thread to finish its work
        waitForCondition {
            exerciseDao.get(exId).weight == 130f
        }

        val updatedEx = exerciseDao.get(exId)
        assertEquals(130f, updatedEx.weight, 0.1f)
    }

    private fun waitForCondition(timeoutMs: Long = 2000, condition: () -> Boolean) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition()) return
            Thread.sleep(50)
        }
        throw AssertionError("Condition was not met within timeout")
    }
}
