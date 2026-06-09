package ca.gainzassist.presentation.add_workout

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
class WorkoutEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WorkoutEntryViewModel

    @Before
    fun setup() {
        viewModel = WorkoutEntryViewModel()
    }

    @Test
    fun defaultExerciseCount_is3() {
        assertEquals("3", viewModel.state.value.numberOfExercises)
    }

    @Test
    fun blankWorkoutName_emitsContinueEventWithoutError() = runTest {
        val events = mutableListOf<WorkoutEntryViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onWorkoutNameChanged("   ")
        viewModel.onContinueClicked()

        val event = events.firstOrNull() as? WorkoutEntryViewModelEvent.ContinueToExercises
        assertNotNull(event)
        assertEquals("", event?.workoutName) // Trimmed
        assertEquals(3, event?.numberOfExercises)
        assertNull(viewModel.state.value.workoutNameError)
        assertNull(viewModel.state.value.numberOfExercisesError)
        
        job.cancel()
    }

    @Test
    fun validSubmit_emitsContinueToExercises() = runTest {
        val events = mutableListOf<WorkoutEntryViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onWorkoutNameChanged("Leg Day")
        viewModel.onNumberOfExercisesChanged("5")
        viewModel.onContinueClicked()

        val event = events.firstOrNull() as? WorkoutEntryViewModelEvent.ContinueToExercises
        assertNotNull(event)
        assertEquals("Leg Day", event?.workoutName)
        assertEquals(5, event?.numberOfExercises)
        
        job.cancel()
    }

    @Test
    fun invalidExerciseCount_handledSafely() = runTest {
        val events = mutableListOf<WorkoutEntryViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onNumberOfExercisesChanged("abc")
        viewModel.onContinueClicked()

        assertTrue(events.isEmpty())
        assertNotNull(viewModel.state.value.numberOfExercisesError)
        
        job.cancel()
    }
}
