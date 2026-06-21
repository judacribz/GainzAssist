package ca.gainzassist.feature.start_workout.presentation.viewmodel

import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.session.SessionProgressSnapshot
import ca.gainzassist.domain.usecase.session.GetSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteWorkoutUseCase
import ca.gainzassist.domain.usecase.session.RemoveSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.SaveSessionProgressUseCase
import ca.gainzassist.domain.usecase.workout.InsertCompletedSessionUseCase
import ca.gainzassist.feature.start_workout.domain.usecase.FinishWorkoutSessionUseCase
import ca.gainzassist.test.fakes.FakeSessionPreferencesRepository
import ca.gainzassist.test.fakes.FakeWorkoutRepository
import ca.gainzassist.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WorkoutScreenViewModel
    private lateinit var sessionPreferencesRepository: FakeSessionPreferencesRepository
    private lateinit var fakeWorkoutRepository: FakeWorkoutRepository

    private fun <T> assertNotNullValue(value: T?): T {
        assertTrue(value != null)
        return value ?: error("Expected non-null value")
    }

    @Before
    fun setup() {
        sessionPreferencesRepository = FakeSessionPreferencesRepository()
        fakeWorkoutRepository = FakeWorkoutRepository()

        val insertCompletedSessionUseCase = InsertCompletedSessionUseCase(fakeWorkoutRepository)
        val removeIncompleteWorkoutUseCase = RemoveIncompleteWorkoutUseCase(sessionPreferencesRepository)
        val removeIncompleteSessionUseCase = RemoveIncompleteSessionUseCase(sessionPreferencesRepository)
        val removeSessionProgressUseCase = RemoveSessionProgressUseCase(sessionPreferencesRepository)

        val finishWorkoutSessionUseCase = FinishWorkoutSessionUseCase(
            insertCompletedSessionUseCase = insertCompletedSessionUseCase,
            removeIncompleteWorkoutUseCase = removeIncompleteWorkoutUseCase,
            removeIncompleteSessionUseCase = removeIncompleteSessionUseCase,
            removeSessionProgressUseCase = removeSessionProgressUseCase
        )

        viewModel = WorkoutScreenViewModel(
            getSessionProgressUseCase = GetSessionProgressUseCase(sessionPreferencesRepository),
            saveSessionProgressUseCase = SaveSessionProgressUseCase(sessionPreferencesRepository),
            finishWorkoutSessionUseCase = finishWorkoutSessionUseCase
        )
    }

    @Test
    fun getSessionProgress_returnsNullWhenNoProgressSaved() = runTest {
        var loadedSnapshot: SessionProgressSnapshot? = null
        viewModel.getSessionProgress("My Workout") {
            loadedSnapshot = it
        }
        advanceUntilIdle()
        assertNull(loadedSnapshot)
    }

    @Test
    fun saveSessionProgress_writesLegacyJsonShape() = runTest {
        val snapshot = SessionProgressSnapshot(
            exerciseProgress = mapOf(0 to 1, 1 to 2),
            setProgress = mapOf(0 to 1)
        )
        val workoutName = "Push Day"

        viewModel.saveSessionProgress(workoutName, snapshot)
        advanceUntilIdle()

        val savedJsonNullable = sessionPreferencesRepository.getSessionProgress(workoutName)
        val savedJson = assertNotNullValue(savedJsonNullable)

        // Assert it contains expected legacy keys
        assertTrue(savedJson.contains("\"exercise progress\""))
        assertTrue(savedJson.contains("\"set progress\""))
    }

    @Test
    fun getSessionProgress_readsSavedSnapshot() = runTest {
        val snapshot = SessionProgressSnapshot(
            exerciseProgress = mapOf(0 to 1, 1 to 2),
            setProgress = mapOf(0 to 1)
        )
        val workoutName = "Push Day"

        viewModel.saveSessionProgress(workoutName, snapshot)
        advanceUntilIdle()

        var loadedSnapshotNullable: SessionProgressSnapshot? = null
        viewModel.getSessionProgress(workoutName) {
            loadedSnapshotNullable = it
        }
        advanceUntilIdle()

        val loadedSnapshot = assertNotNullValue(loadedSnapshotNullable)

        assertEquals(snapshot.exerciseProgress, loadedSnapshot.exerciseProgress)
        assertEquals(snapshot.setProgress, loadedSnapshot.setProgress)
    }

    @Test
    fun getSessionProgress_handlesInvalidJsonSafely() = runTest {
        val workoutName = "Broken Workout"
        // Force invalid JSON string using the repository directly
        sessionPreferencesRepository.saveSessionProgress(workoutName, "invalid json string")

        var loadedSnapshotNullable: SessionProgressSnapshot? = null
        viewModel.getSessionProgress(workoutName) {
            loadedSnapshotNullable = it
        }
        advanceUntilIdle()

        val loadedSnapshot = assertNotNullValue(loadedSnapshotNullable)

        // Misc.readValue catches the exception and returns an empty map.
        // SessionProgressMapper.fromLegacyMap(emptyMap) should return empty snapshot.
        assertTrue(loadedSnapshot.exerciseProgress.isEmpty())
        assertTrue(loadedSnapshot.setProgress.isEmpty())
    }

    @Test
    fun saveSessionProgress_preservesNullValues() = runTest {
        val snapshot = SessionProgressSnapshot(
            exerciseProgress = mapOf(0 to null, 1 to 2),
            setProgress = mapOf(0 to null)
        )
        val workoutName = "Null Test"

        viewModel.saveSessionProgress(workoutName, snapshot)
        advanceUntilIdle()

        var loadedSnapshotNullable: SessionProgressSnapshot? = null
        viewModel.getSessionProgress(workoutName) {
            loadedSnapshotNullable = it
        }
        advanceUntilIdle()

        val loadedSnapshot = assertNotNullValue(loadedSnapshotNullable)

        assertEquals(snapshot.exerciseProgress, loadedSnapshot.exerciseProgress)
        assertEquals(snapshot.setProgress, loadedSnapshot.setProgress)
    }

    @Test
    fun clearFinishedWorkoutState_removesIncompleteAndProgress() = runTest {
        val workoutName = "Push Day"
        sessionPreferencesRepository.addIncompleteWorkout(workoutName)
        sessionPreferencesRepository.saveIncompleteSession(workoutName, "{\"session\":true}")
        sessionPreferencesRepository.saveSessionProgress(workoutName, "{\"progress\":true}")

        viewModel.finishWorkoutSession(workoutName, Session())
        advanceUntilIdle()

        assertFalse(sessionPreferencesRepository.getIncompleteWorkoutNames().contains(workoutName))
        assertEquals(null, sessionPreferencesRepository.getIncompleteSession(workoutName))
        assertEquals(null, sessionPreferencesRepository.getSessionProgress(workoutName))
    }

    @Test
    fun clearFinishedWorkoutState_alwaysClearsProgress() = runTest {
        val workoutName = "Pull Day"
        sessionPreferencesRepository.saveIncompleteSession(workoutName, "{\"session\":true}")
        sessionPreferencesRepository.saveSessionProgress(workoutName, "{\"progress\":true}")

        viewModel.finishWorkoutSession(workoutName, Session())
        advanceUntilIdle()

        assertEquals(null, sessionPreferencesRepository.getSessionProgress(workoutName))
        assertEquals("{\"session\":true}", sessionPreferencesRepository.getIncompleteSession(workoutName))
    }

    @Test
    fun insertCompletedSession_delegatesToUseCase() = runTest {
        val session = Session().apply { workoutName = "Chest Day" }
        viewModel.finishWorkoutSession("Chest Day", session)
        advanceUntilIdle()

        assertEquals(1, fakeWorkoutRepository.insertedSessions.size)
        assertEquals(session, fakeWorkoutRepository.insertedSessions.first())
        assertEquals(true, fakeWorkoutRepository.completedSessionSyncFlags.first())
    }

    @Test
    fun insertCompletedSession_doesNotModifySessionWhenRepositoryFakeOnlyRecords() = runTest {
        val session = Session().apply { workoutName = "Leg Day" }
        viewModel.finishWorkoutSession("Leg Day", session)
        advanceUntilIdle()

        val recorded = fakeWorkoutRepository.insertedSessions.first()
        assertEquals("Leg Day", recorded.workoutName)
    }
}
