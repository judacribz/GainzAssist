package ca.gainzassist.presentation.add_workout

import ca.gainzassist.activities.add_workout.exercises_entry.ExercisesEntryViewModel
import ca.gainzassist.activities.add_workout.exercises_entry.ExercisesEntryViewModelEvent
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.usecase.workout.ExerciseExistsUseCase
import ca.gainzassist.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ExercisesEntryViewModel

    @Before
    fun setup() {
        val exerciseExistsUseCase = ExerciseExistsUseCase()

        viewModel = ExercisesEntryViewModel(exerciseExistsUseCase)
    }

    @Test
    fun initialSelectedIndex_isFirstTab() {
        viewModel.initialize("Leg Day", 3)

        assertEquals(0, viewModel.state.value.selectedIndex)
        assertEquals(3, viewModel.state.value.numberOfExercises)
        assertEquals(3, viewModel.state.value.exercises.size)
        assertEquals(0, viewModel.state.value.enteredExerciseCount)
    }

    @Test
    fun submittingExercise_storesItAndUpdatesEnteredCount() {
        viewModel.initialize("Leg Day", 3)

        val exercise = Exercise().apply {
            exerciseNumber = 0
            name = "Squats"
        }

        viewModel.onExerciseSubmitted(exercise)

        assertEquals(1, viewModel.state.value.enteredExerciseCount)
        assertEquals("Squats", viewModel.state.value.exercises[0].name)
    }

    @Test
    fun whenAllExercisesEntered_goToSummaryEventIsEmitted() = runTest {
        viewModel.initialize("Leg Day", 2)
        
        val events = mutableListOf<ExercisesEntryViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        // Submit first
        viewModel.onExerciseSubmitted(Exercise().apply { exerciseNumber = 0; name = "Squat" })
        assertTrue(events.isEmpty())

        // Submit second
        viewModel.onExerciseSubmitted(Exercise().apply { exerciseNumber = 1; name = "Lunge" })
        
        val event = events.firstOrNull() as? ExercisesEntryViewModelEvent.GoToSummary
        assertNotNull(event)
        assertEquals("Leg Day", event?.workoutName)
        assertEquals(2, event?.exercises?.size)
        
        job.cancel()
    }

    @Test
    fun onExerciseDeleted_removesExerciseAndUpdatesCounts() {
        viewModel.initialize("Leg Day", 3)
        viewModel.onExerciseSubmitted(Exercise().apply { exerciseNumber = 0; name = "Squat" })
        
        assertEquals(1, viewModel.state.value.enteredExerciseCount)
        assertEquals(3, viewModel.state.value.numberOfExercises)

        viewModel.onExerciseDeleted(0)

        assertEquals(0, viewModel.state.value.enteredExerciseCount)
        assertEquals(2, viewModel.state.value.numberOfExercises)
        assertEquals(2, viewModel.state.value.exercises.size)
    }

    @Test
    fun submittingExercise_advancesToNextEmptyTab() {
        viewModel.initialize("Leg Day", 3)
        
        assertEquals(0, viewModel.state.value.selectedIndex)

        viewModel.onExerciseSubmitted(Exercise().apply { exerciseNumber = 0; name = "Squat" })
        
        assertEquals(1, viewModel.state.value.selectedIndex)
        
        viewModel.onTabSelected(2)
        viewModel.onExerciseSubmitted(Exercise().apply { exerciseNumber = 2; name = "Lunge" })
        
        assertEquals(1, viewModel.state.value.selectedIndex)
    }
}
