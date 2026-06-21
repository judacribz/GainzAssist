package ca.gainzassist.feature.exercises_entry.presentation.viewmodel

import ca.gainzassist.feature.exercises_entry.domain.usecase.BuildExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.BuildWorkoutFromExerciseEntriesUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.CheckDuplicateExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.DeleteExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.ValidateExerciseInputUseCase
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
        val validateExerciseInputUseCase = ValidateExerciseInputUseCase()
        val checkDuplicateExerciseUseCase = CheckDuplicateExerciseUseCase()
        val buildExerciseUseCase = BuildExerciseUseCase()
        val deleteExerciseUseCase = DeleteExerciseUseCase()
        val buildWorkoutFromExerciseEntriesUseCase = BuildWorkoutFromExerciseEntriesUseCase()

        viewModel = ExercisesEntryViewModel(
            validateExerciseInputUseCase = validateExerciseInputUseCase,
            checkDuplicateExerciseUseCase = checkDuplicateExerciseUseCase,
            buildExerciseUseCase = buildExerciseUseCase,
            deleteExerciseUseCase = deleteExerciseUseCase,
            buildWorkoutFromExerciseEntriesUseCase = buildWorkoutFromExerciseEntriesUseCase
        )
    }

    @Test
    fun initialSelectedIndex_isFirstTab() {
        viewModel.initialize("Leg Day", 3, "10", "3", "45.0", "Barbell")

        assertEquals(0, viewModel.state.value.selectedIndex)
        assertEquals(3, viewModel.state.value.numberOfExercises)
        assertEquals(3, viewModel.state.value.exercises.size)
        assertEquals(0, viewModel.state.value.enteredExerciseCount)
    }

    @Test
    fun submittingExercise_storesItAndUpdatesEnteredCount() {
        viewModel.initialize("Leg Day", 3, "10", "3", "45.0", "Barbell")

        viewModel.onExerciseNameChanged(0, "Squats")
        viewModel.onWeightChanged(0, "135.0")
        viewModel.onRepsChanged(0, "10")
        viewModel.onSetsChanged(0, "3")
        viewModel.onExerciseSubmitted(0)

        assertEquals(1, viewModel.state.value.enteredExerciseCount)
        assertEquals("Squats", viewModel.state.value.exercises[0].name)
    }

    @Test
    fun whenAllExercisesEntered_goToSummaryEventIsEmitted() = runTest {
        viewModel.initialize("Leg Day", 2, "10", "3", "45.0", "Barbell")

        val events = mutableListOf<ExercisesEntryViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        // Submit first
        viewModel.onExerciseNameChanged(0, "Squat")
        viewModel.onWeightChanged(0, "135.0")
        viewModel.onRepsChanged(0, "10")
        viewModel.onSetsChanged(0, "3")
        viewModel.onExerciseSubmitted(0)
        assertTrue(events.isEmpty())

        // Submit second
        viewModel.onExerciseNameChanged(1, "Lunge")
        viewModel.onWeightChanged(1, "135.0")
        viewModel.onRepsChanged(1, "10")
        viewModel.onSetsChanged(1, "3")
        viewModel.onExerciseSubmitted(1)

        val event = events.firstOrNull() as? ExercisesEntryViewModelEvent.GoToSummary
        assertNotNull(event)
        assertEquals("Leg Day", event?.workout?.name)
        assertEquals(2, event?.workout?.exercises?.size)

        job.cancel()
    }

    @Test
    fun onExerciseDeleted_removesExerciseAndUpdatesCounts() {
        viewModel.initialize("Leg Day", 3, "10", "3", "45.0", "Barbell")
        viewModel.onExerciseNameChanged(0, "Squat")
        viewModel.onWeightChanged(0, "135.0")
        viewModel.onRepsChanged(0, "10")
        viewModel.onSetsChanged(0, "3")
        viewModel.onExerciseSubmitted(0)

        assertEquals(1, viewModel.state.value.enteredExerciseCount)
        assertEquals(3, viewModel.state.value.numberOfExercises)

        viewModel.onExerciseDeleted(0)

        assertEquals(0, viewModel.state.value.enteredExerciseCount)
        assertEquals(2, viewModel.state.value.numberOfExercises)
        assertEquals(2, viewModel.state.value.exercises.size)
    }

    @Test
    fun submittingExercise_advancesToNextEmptyTab() {
        viewModel.initialize("Leg Day", 3, "10", "3", "45.0", "Barbell")

        assertEquals(0, viewModel.state.value.selectedIndex)

        viewModel.onExerciseNameChanged(0, "Squat")
        viewModel.onWeightChanged(0, "135.0")
        viewModel.onRepsChanged(0, "10")
        viewModel.onSetsChanged(0, "3")
        viewModel.onExerciseSubmitted(0)

        assertEquals(1, viewModel.state.value.selectedIndex)

        viewModel.onTabSelected(2)
        viewModel.onExerciseNameChanged(2, "Lunge")
        viewModel.onWeightChanged(2, "135.0")
        viewModel.onRepsChanged(2, "10")
        viewModel.onSetsChanged(2, "3")
        viewModel.onExerciseSubmitted(2)

        assertEquals(1, viewModel.state.value.selectedIndex)
    }

    @Test
    fun submittingDuplicateExercise_showsDuplicateErrorAndDoesNotSubmit() {
        viewModel.initialize("Leg Day", 3, "10", "3", "45.0", "Barbell")

        // Submit first exercise
        viewModel.onExerciseNameChanged(0, "Squats")
        viewModel.onWeightChanged(0, "135.0")
        viewModel.onRepsChanged(0, "10")
        viewModel.onSetsChanged(0, "3")
        viewModel.onExerciseSubmitted(0)

        // Attempt to submit duplicate exercise in tab 1
        viewModel.onExerciseNameChanged(1, "Squats")
        viewModel.onWeightChanged(1, "135.0")
        viewModel.onRepsChanged(1, "10")
        viewModel.onSetsChanged(1, "3")
        viewModel.onExerciseSubmitted(1)

        assertTrue(viewModel.state.value.exerciseInputs[1].hasDuplicateError)
        assertEquals(1, viewModel.state.value.enteredExerciseCount)
    }
}
