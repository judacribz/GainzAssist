package ca.gainzassist.presentation.add_workout

import ca.gainzassist.domain.usecase.workout.SaveWorkoutUseCase
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Workout
import ca.gainzassist.test.fakes.FakeWorkoutRepository
import ca.gainzassist.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SummaryViewModel
    private lateinit var fakeWorkoutRepository: FakeWorkoutRepository
    private lateinit var saveWorkoutUseCase: SaveWorkoutUseCase

    @Before
    fun setup() {
        fakeWorkoutRepository = FakeWorkoutRepository()
        saveWorkoutUseCase = SaveWorkoutUseCase(fakeWorkoutRepository)

        viewModel = SummaryViewModel(saveWorkoutUseCase)
    }

    @Test
    fun save_callsSaveWorkoutUseCase_andEmitsSaved() = runTest {
        val events = mutableListOf<SummaryViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.initialize(
            initialWorkout = null,
            workoutName = "Push Day",
            exercises = listOf(Exercise().apply { name = "Bench Press" })
        )

        viewModel.onSaveClicked(isUpdate = false)

        val savedWorkout = fakeWorkoutRepository.getWorkoutByName("Push Day")
        assertNotNull(savedWorkout)
        assertEquals("Push Day", savedWorkout?.name)

        assertTrue(events.contains(SummaryViewModelEvent.Saved))
        
        job.cancel()
    }

    @Test
    fun updateMode_preservesUpdateBehavior() = runTest {
        val existingWorkout = Workout().apply {
            id = 1L
            name = "Push Day"
        }
        fakeWorkoutRepository.insertWorkout(existingWorkout)

        viewModel.initialize(
            initialWorkout = existingWorkout,
            workoutName = "Push Day Updated",
            exercises = listOf(Exercise().apply { name = "Incline Press" })
        )

        viewModel.onSaveClicked(isUpdate = true)

        // Verify it didn't create a new workout but updated existing
        val updatedWorkout = fakeWorkoutRepository.getWorkoutByName("Push Day Updated")
        assertNotNull(updatedWorkout)
        assertEquals(1L, updatedWorkout?.id)
        
        val oldWorkout = fakeWorkoutRepository.getWorkoutByName("Push Day")
        assertNull(oldWorkout)
    }

    @Test
    fun saveFailure_emptyWorkoutName_setsErrorMessage() = runTest {
        viewModel.initialize(
            initialWorkout = null,
            workoutName = "",
            exercises = listOf(Exercise().apply { name = "Bench Press" })
        )

        viewModel.onSaveClicked(isUpdate = false)

        assertEquals("Workout name is required.", viewModel.state.value.errorMessage)
    }

    @Test
    fun saveFailure_noExercises_setsErrorMessage() = runTest {
        viewModel.initialize(
            initialWorkout = null,
            workoutName = "Push Day",
            exercises = emptyList()
        )

        viewModel.onSaveClicked(isUpdate = false)

        assertEquals("No exercises added.", viewModel.state.value.errorMessage)
    }
}
