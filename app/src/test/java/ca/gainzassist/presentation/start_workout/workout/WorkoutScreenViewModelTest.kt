package ca.gainzassist.presentation.start_workout.workout

import ca.gainzassist.domain.session.SessionProgressSnapshot
import ca.gainzassist.domain.usecase.session.GetSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.SaveSessionProgressUseCase
import ca.gainzassist.test.fakes.FakeSessionPreferencesRepository
import ca.gainzassist.test.rules.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WorkoutScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeSessionPreferencesRepository
    private lateinit var getSessionProgressUseCase: GetSessionProgressUseCase
    private lateinit var saveSessionProgressUseCase: SaveSessionProgressUseCase
    private lateinit var viewModel: WorkoutScreenViewModel

    private fun <T> assertNotNullValue(value: T?): T {
        assertTrue(value != null)
        return value ?: error("Expected non-null value")
    }

    @Before
    fun setup() {
        fakeRepository = FakeSessionPreferencesRepository()
        getSessionProgressUseCase = GetSessionProgressUseCase(fakeRepository)
        saveSessionProgressUseCase = SaveSessionProgressUseCase(fakeRepository)
        viewModel = WorkoutScreenViewModel(getSessionProgressUseCase, saveSessionProgressUseCase)
    }

    @Test
    fun getSessionProgress_returnsNullWhenNoProgressSaved() = runTest {
        val progress = viewModel.getSessionProgress("My Workout")
        assertNull(progress)
    }

    @Test
    fun saveSessionProgress_writesLegacyJsonShape() = runTest {
        val snapshot = SessionProgressSnapshot(
            exerciseProgress = mapOf(0 to 1, 1 to 2),
            setProgress = mapOf(0 to 1)
        )
        val workoutName = "Push Day"

        viewModel.saveSessionProgress(workoutName, snapshot)

        val savedJsonNullable = fakeRepository.getSessionProgress(workoutName)
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
        val loadedSnapshotNullable = viewModel.getSessionProgress(workoutName)
        val loadedSnapshot = assertNotNullValue(loadedSnapshotNullable)

        assertEquals(snapshot.exerciseProgress, loadedSnapshot.exerciseProgress)
        assertEquals(snapshot.setProgress, loadedSnapshot.setProgress)
    }

    @Test
    fun getSessionProgress_handlesInvalidJsonSafely() = runTest {
        val workoutName = "Broken Workout"
        // Force invalid JSON string using the repository directly
        fakeRepository.saveSessionProgress(workoutName, "invalid json string")

        val loadedSnapshotNullable = viewModel.getSessionProgress(workoutName)
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
        val loadedSnapshotNullable = viewModel.getSessionProgress(workoutName)
        val loadedSnapshot = assertNotNullValue(loadedSnapshotNullable)

        assertEquals(snapshot.exerciseProgress, loadedSnapshot.exerciseProgress)
        assertEquals(snapshot.setProgress, loadedSnapshot.setProgress)
    }
}
