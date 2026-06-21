package ca.gainzassist.feature.main.presentation.viewmodel

import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.usecase.session.GetIncompleteWorkoutNamesUseCase
import ca.gainzassist.domain.usecase.workout.DeleteAllWorkoutsUseCase
import ca.gainzassist.domain.usecase.workout.DeleteWorkoutUseCase
import ca.gainzassist.domain.usecase.workout.GetWorkoutWithExercisesByNameUseCase
import ca.gainzassist.domain.usecase.workout.ObserveWorkoutsUseCase
import ca.gainzassist.feature.main.presentation.screen.MainTab
import ca.gainzassist.test.fakes.FakeSessionPreferencesRepository
import ca.gainzassist.test.fakes.FakeWorkoutRepository
import ca.gainzassist.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MainViewModel
    private lateinit var fakeWorkoutRepository: FakeWorkoutRepository
    private lateinit var fakeSessionPreferencesRepository: FakeSessionPreferencesRepository

    @Before
    fun setup() {
        fakeWorkoutRepository = FakeWorkoutRepository()
        fakeSessionPreferencesRepository = FakeSessionPreferencesRepository()

        val observeWorkoutsUseCase = ObserveWorkoutsUseCase(fakeWorkoutRepository)
        val getWorkoutWithExercisesByNameUseCase = GetWorkoutWithExercisesByNameUseCase(fakeWorkoutRepository)
        val deleteWorkoutUseCase = DeleteWorkoutUseCase(fakeWorkoutRepository)
        val deleteAllWorkoutsUseCase = DeleteAllWorkoutsUseCase(fakeWorkoutRepository)
        val getIncompleteWorkoutNamesUseCase = GetIncompleteWorkoutNamesUseCase(fakeSessionPreferencesRepository)

        viewModel = MainViewModel(
            observeWorkoutsUseCase,
            getWorkoutWithExercisesByNameUseCase,
            deleteWorkoutUseCase,
            deleteAllWorkoutsUseCase,
            getIncompleteWorkoutNamesUseCase
        )
    }

    @Test
    fun initialState_loadsWorkoutNames() = runTest {
        assertTrue(viewModel.state.value.allWorkoutNames.isEmpty())

        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Chest Day" })
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Leg Day" })

        assertEquals(listOf("Chest Day", "Leg Day"), viewModel.state.value.allWorkoutNames)
    }

    @Test
    fun search_filtersWorkoutNames() = runTest {
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Chest Day" })
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Leg Day" })

        viewModel.onSearchQueryChanged("chest")

        assertEquals(listOf("Chest Day"), viewModel.state.value.filteredWorkoutNames)
    }

    @Test
    fun addWorkoutClick_emitsAddWorkoutEvent() = runTest {
        val events = mutableListOf<MainViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAddWorkoutClicked()

        assertTrue(events.contains(MainViewModelEvent.AddWorkout))
        job.cancel()
    }

    @Test
    fun workoutClick_emitsStartWorkoutEvent() = runTest {
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Chest Day" })

        val events = mutableListOf<MainViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onWorkoutClicked("Chest Day")

        val event = events.firstOrNull() as? MainViewModelEvent.StartWorkout
        assertTrue(event != null && event.workout.name == "Chest Day")
        job.cancel()
    }

    @Test
    fun longClick_setsSelectedWorkoutName() = runTest {
        viewModel.onWorkoutLongClicked("Chest Day")
        assertEquals("Chest Day", viewModel.state.value.selectedWorkoutName)
    }

    @Test
    fun deleteWorkout_removesWorkoutAndRefreshesList() = runTest {
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Chest Day" })
        assertEquals(listOf("Chest Day"), viewModel.state.value.allWorkoutNames)

        viewModel.onDeleteWorkoutClicked("Chest Day")

        assertTrue(viewModel.state.value.allWorkoutNames.isEmpty())
        assertNull(viewModel.state.value.selectedWorkoutName)
    }

    @Test
    fun logout_emitsLoggedOutEvent() = runTest {
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Chest Day" })

        val events = mutableListOf<MainViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onLogoutClicked()

        assertTrue(viewModel.state.value.allWorkoutNames.isEmpty())
        assertTrue(events.contains(MainViewModelEvent.LoggedOut))
        job.cancel()
    }

    @Test
    fun refreshResumeWorkouts_addsIncompleteWorkoutToState() = runTest {
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Push Day" })
        assertTrue(viewModel.state.value.resumeWorkoutNames.isEmpty())

        fakeSessionPreferencesRepository.addIncompleteWorkout("Push Day")
        viewModel.refreshResumeWorkouts()

        assertTrue(viewModel.state.value.resumeWorkoutNames.contains("Push Day"))
    }

    @Test
    fun refreshResumeWorkouts_removesCompletedWorkoutFromState() = runTest {
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Push Day" })
        fakeSessionPreferencesRepository.addIncompleteWorkout("Push Day")

        viewModel.refreshResumeWorkouts()
        assertTrue(viewModel.state.value.resumeWorkoutNames.contains("Push Day"))

        fakeSessionPreferencesRepository.removeIncompleteWorkout("Push Day")
        viewModel.refreshResumeWorkouts()

        assertTrue(!viewModel.state.value.resumeWorkoutNames.contains("Push Day"))
    }

    @Test
    fun selectingResumeTab_refreshesIncompleteNames() = runTest {
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Push Day" })
        fakeSessionPreferencesRepository.addIncompleteWorkout("Push Day")

        viewModel.onTabSelected(MainTab.RESUME)

        assertTrue(viewModel.state.value.resumeWorkoutNames.contains("Push Day"))
    }

    @Test
    fun onResumeStyleRefresh_doesNotAffectSearchOrWorkoutList() = runTest {
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Push Day" })
        fakeWorkoutRepository.insertWorkout(Workout().apply { name = "Pull Day" })

        viewModel.onSearchQueryChanged("pull")

        assertEquals(listOf("Pull Day"), viewModel.state.value.filteredWorkoutNames)
        assertEquals("pull", viewModel.state.value.searchQuery)

        fakeSessionPreferencesRepository.addIncompleteWorkout("Push Day")
        viewModel.refreshResumeWorkouts()

        assertTrue(viewModel.state.value.resumeWorkoutNames.contains("Push Day"))
        assertEquals(listOf("Pull Day"), viewModel.state.value.filteredWorkoutNames)
        assertEquals("pull", viewModel.state.value.searchQuery)
    }
}
