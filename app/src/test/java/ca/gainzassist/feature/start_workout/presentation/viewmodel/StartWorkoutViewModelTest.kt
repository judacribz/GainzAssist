package ca.gainzassist.feature.start_workout.presentation.viewmodel

import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.usecase.session.AddIncompleteWorkoutUseCase
import ca.gainzassist.domain.usecase.session.GetIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteWorkoutUseCase
import ca.gainzassist.domain.usecase.session.SaveIncompleteSessionUseCase
import ca.gainzassist.feature.start_workout.domain.model.StartWorkoutRestoreDecision
import ca.gainzassist.feature.start_workout.domain.usecase.SaveIncompleteWorkoutUseCase
import ca.gainzassist.feature.start_workout.domain.usecase.StartWorkoutSessionUseCase
import ca.gainzassist.feature.start_workout.presentation.screen.StartWorkoutTab
import ca.gainzassist.test.fakes.FakeSessionPreferencesRepository
import ca.gainzassist.test.fakes.FakeWorkoutRepository
import ca.gainzassist.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartWorkoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: StartWorkoutViewModel
    private lateinit var workoutRepository: FakeWorkoutRepository
    private lateinit var sessionPreferencesRepository: FakeSessionPreferencesRepository

    @Before
    fun setup() {
        workoutRepository = FakeWorkoutRepository()
        sessionPreferencesRepository = FakeSessionPreferencesRepository()

        val startWorkoutSessionUseCase = StartWorkoutSessionUseCase(
            removeIncompleteWorkoutUseCase = RemoveIncompleteWorkoutUseCase(sessionPreferencesRepository),
            getIncompleteSessionUseCase = GetIncompleteSessionUseCase(sessionPreferencesRepository),
            removeIncompleteSessionUseCase = RemoveIncompleteSessionUseCase(sessionPreferencesRepository)
        )

        val saveIncompleteWorkoutUseCase = SaveIncompleteWorkoutUseCase(
            saveIncompleteSessionUseCase = SaveIncompleteSessionUseCase(sessionPreferencesRepository),
            addIncompleteWorkoutUseCase = AddIncompleteWorkoutUseCase(sessionPreferencesRepository)
        )

        viewModel = StartWorkoutViewModel(
            startWorkoutSessionUseCase = startWorkoutSessionUseCase,
            saveIncompleteWorkoutUseCase = saveIncompleteWorkoutUseCase
        )
    }

    @Test
    fun initializeFromWorkout_setsDefaultTabsAndExercises() = runTest {
        val exercise = Exercise()
        exercise.name = "Squat"
        val workout = Workout("Leg Day", arrayListOf(exercise))

        viewModel.initializeFromWorkout(workout)

        val state = viewModel.state.value
        assertEquals("Leg Day", state.workoutName)
        assertEquals(StartWorkoutTab.WORKOUT, state.selectedTab)
        assertEquals(listOf(StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES), state.availableTabs)
        assertEquals(listOf(exercise), state.exercises)
    }

    @Test
    fun onWarmupsGenerated_emptyWarmups_keepsWorkoutAndExercisesTabs() = runTest {
        viewModel.onWarmupsGenerated(emptyList())
        val state = viewModel.state.value
        assertEquals(listOf(StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES), state.availableTabs)
        assertEquals(StartWorkoutTab.WORKOUT, state.selectedTab)
        assertTrue(state.warmups.isEmpty())
    }

    @Test
    fun onWarmupsGenerated_withWarmups_addsWarmupsTab() = runTest {
        val stretching = Exercise()
        stretching.name = "Stretching"
        val warmups = listOf(stretching)
        viewModel.onWarmupsGenerated(warmups)
        val state = viewModel.state.value
        assertEquals(
            listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
            state.availableTabs
        )
        assertEquals(StartWorkoutTab.WORKOUT, state.selectedTab)
        assertEquals(warmups, state.warmups)
    }

    @Test
    fun prepareSessionRestore_blankWorkoutName_returnsStartFresh() = runTest {
        val result = viewModel.prepareSessionRestore("  ")
        assertTrue(result is StartWorkoutRestoreDecision.StartFresh)
    }

    @Test
    fun prepareSessionRestore_noIncompleteWorkout_returnsStartFresh() = runTest {
        val result = viewModel.prepareSessionRestore("Push Day")
        assertTrue(result is StartWorkoutRestoreDecision.StartFresh)
    }

    @Test
    fun prepareSessionRestore_incompleteWorkoutNoSession_removesAndStartsFresh() = runTest {
        sessionPreferencesRepository.addIncompleteWorkout("Push Day")

        val result = viewModel.prepareSessionRestore("Push Day")

        assertTrue(result is StartWorkoutRestoreDecision.StartFresh)
        val incompleteWorkouts = sessionPreferencesRepository.getIncompleteWorkoutNames()
        assertFalse(incompleteWorkouts.contains("Push Day"))
    }

    @Test
    fun prepareSessionRestore_withSessionJson_returnsRestoreFromJsonAndCleansSession() = runTest {
        sessionPreferencesRepository.addIncompleteWorkout("Push Day")
        sessionPreferencesRepository.saveIncompleteSession("Push Day", "{\"key\":\"val\"}")

        val result = viewModel.prepareSessionRestore("Push Day")

        assertTrue(result is StartWorkoutRestoreDecision.RestoreFromJson)
        assertEquals("{\"key\":\"val\"}", (result as StartWorkoutRestoreDecision.RestoreFromJson).sessionJson)

        val incompleteWorkouts = sessionPreferencesRepository.getIncompleteWorkoutNames()
        assertFalse(incompleteWorkouts.contains("Push Day"))

        val savedSession = sessionPreferencesRepository.getIncompleteSession("Push Day")
        assertEquals(null, savedSession)
    }

    @Test
    fun saveLeavingSession_nonEmptyJson_savesSessionAndIncompleteWorkout() = runTest {
        viewModel.saveLeavingSession("Push Day", "{\"key\":\"val\"}")

        val savedSession = sessionPreferencesRepository.getIncompleteSession("Push Day")
        assertEquals("{\"key\":\"val\"}", savedSession)

        val incompleteWorkouts = sessionPreferencesRepository.getIncompleteWorkoutNames()
        assertTrue(incompleteWorkouts.contains("Push Day"))
    }

    @Test
    fun saveLeavingSession_emptyJson_addsIncompleteWorkoutOnly() = runTest {
        viewModel.saveLeavingSession("Push Day", "")

        val savedSession = sessionPreferencesRepository.getIncompleteSession("Push Day")
        assertEquals(null, savedSession)

        val incompleteWorkouts = sessionPreferencesRepository.getIncompleteWorkoutNames()
        assertTrue(incompleteWorkouts.contains("Push Day"))
    }

    @Test
    fun events_onHowToVideosClicked_emitsOpenHowToVideos() = runTest {
        val events = mutableListOf<StartWorkoutViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onHowToVideosClicked()

        assertEquals(1, events.size)
        assertTrue(events[0] is StartWorkoutViewModelEvent.OpenHowToVideos)

        job.cancel()
    }

    @Test
    fun events_onBackClicked_emitsExitWorkout() = runTest {
        val events = mutableListOf<StartWorkoutViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onBackClicked()

        assertEquals(1, events.size)
        assertTrue(events[0] is StartWorkoutViewModelEvent.ExitWorkout)

        job.cancel()
    }

    @Test
    fun events_onFinishWorkoutClicked_emitsFinishWorkout() = runTest {
        val events = mutableListOf<StartWorkoutViewModelEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onFinishWorkoutClicked()

        assertEquals(1, events.size)
        assertTrue(events[0] is StartWorkoutViewModelEvent.FinishWorkout)

        job.cancel()
    }

    @Test
    fun events_onTabSelected_updatesSelectedTab() = runTest {
        viewModel.onTabSelected(StartWorkoutTab.EXERCISES)
        assertEquals(StartWorkoutTab.EXERCISES, viewModel.state.value.selectedTab)
    }
}
