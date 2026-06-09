package ca.gainzassist.domain.usecase.session

import ca.gainzassist.test.fakes.FakeSessionPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionUseCaseTest {

    private lateinit var repository: FakeSessionPreferencesRepository
    private lateinit var getIncompleteWorkoutNamesUseCase: GetIncompleteWorkoutNamesUseCase
    private lateinit var addIncompleteWorkoutUseCase: AddIncompleteWorkoutUseCase
    private lateinit var removeIncompleteWorkoutUseCase: RemoveIncompleteWorkoutUseCase
    private lateinit var saveIncompleteSessionUseCase: SaveIncompleteSessionUseCase
    private lateinit var getIncompleteSessionUseCase: GetIncompleteSessionUseCase
    private lateinit var removeIncompleteSessionUseCase: RemoveIncompleteSessionUseCase
    private lateinit var saveSessionProgressUseCase: SaveSessionProgressUseCase
    private lateinit var getSessionProgressUseCase: GetSessionProgressUseCase
    private lateinit var removeSessionProgressUseCase: RemoveSessionProgressUseCase

    @Before
    fun setup() {
        repository = FakeSessionPreferencesRepository()
        getIncompleteWorkoutNamesUseCase = GetIncompleteWorkoutNamesUseCase(repository)
        addIncompleteWorkoutUseCase = AddIncompleteWorkoutUseCase(repository)
        removeIncompleteWorkoutUseCase = RemoveIncompleteWorkoutUseCase(repository)
        saveIncompleteSessionUseCase = SaveIncompleteSessionUseCase(repository)
        getIncompleteSessionUseCase = GetIncompleteSessionUseCase(repository)
        removeIncompleteSessionUseCase = RemoveIncompleteSessionUseCase(repository)
        saveSessionProgressUseCase = SaveSessionProgressUseCase(repository)
        getSessionProgressUseCase = GetSessionProgressUseCase(repository)
        removeSessionProgressUseCase = RemoveSessionProgressUseCase(repository)
    }

    @Test
    fun `incomplete workout use cases handle data correctly`() = runTest {
        assertTrue(getIncompleteWorkoutNamesUseCase().isEmpty())

        addIncompleteWorkoutUseCase("Workout 1")
        addIncompleteWorkoutUseCase("Workout 2")
        
        val names = getIncompleteWorkoutNamesUseCase()
        assertEquals(2, names.size)
        assertTrue(names.contains("Workout 1"))
        assertTrue(names.contains("Workout 2"))

        val removed = removeIncompleteWorkoutUseCase("Workout 1")
        assertTrue(removed)
        assertFalse(getIncompleteWorkoutNamesUseCase().contains("Workout 1"))
        
        val removedAgain = removeIncompleteWorkoutUseCase("Workout 1")
        assertFalse(removedAgain)
    }

    @Test
    fun `incomplete session use cases handle data correctly`() = runTest {
        val workoutName = "My Workout"
        val sessionJson = "{\"id\": 1}"
        
        saveIncompleteSessionUseCase(workoutName, sessionJson)
        assertEquals(sessionJson, getIncompleteSessionUseCase(workoutName))
        
        removeIncompleteSessionUseCase(workoutName)
        assertEquals(null, getIncompleteSessionUseCase(workoutName))
    }

    @Test
    fun `session progress use cases handle data correctly`() = runTest {
        val workoutName = "Progress Workout"
        val progressJson = "{\"progress\": 50}"
        
        saveSessionProgressUseCase(workoutName, progressJson)
        assertEquals(progressJson, getSessionProgressUseCase(workoutName))
        
        removeSessionProgressUseCase(workoutName)
        assertEquals(null, getSessionProgressUseCase(workoutName))
    }

    @Test
    fun `use cases ignore blank workout names`() = runTest {
        addIncompleteWorkoutUseCase(" ")
        assertTrue(getIncompleteWorkoutNamesUseCase().isEmpty())
        
        saveIncompleteSessionUseCase("", "{}")
        assertEquals(null, getIncompleteSessionUseCase(""))
        
        saveSessionProgressUseCase("   ", "{}")
        assertEquals(null, getSessionProgressUseCase("   "))
    }
}
