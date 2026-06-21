package ca.gainzassist.feature.start_workout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.feature.start_workout.domain.usecase.FinishWorkoutSessionUseCase
import ca.gainzassist.feature.start_workout.presentation.event.WorkoutScreenEvent
import ca.gainzassist.feature.start_workout.presentation.state.WorkoutScreenState
import ca.gainzassist.core.util.Misc
import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.session.SessionProgressMapper
import ca.gainzassist.domain.session.SessionProgressSnapshot
import ca.gainzassist.domain.usecase.session.GetSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.SaveSessionProgressUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutScreenViewModel(
    private val getSessionProgressUseCase: GetSessionProgressUseCase,
    private val saveSessionProgressUseCase: SaveSessionProgressUseCase,
    private val finishWorkoutSessionUseCase: FinishWorkoutSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutScreenState())
    val uiState: StateFlow<WorkoutScreenState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WorkoutScreenEvent>()
    val events: SharedFlow<WorkoutScreenEvent> = _events.asSharedFlow()

    fun initialize() {
        // TODO: load initial session data
    }

    fun onTimerClicked() {
        // TODO: toggle timer
    }

    fun onRepsChanged(value: String) {
        _uiState.value = _uiState.value.copy(repsText = value)
    }

    fun onWeightChanged(value: String) {
        _uiState.value = _uiState.value.copy(weightText = value)
    }

    fun onFinishSetClicked() {
        // TODO: complete current set
    }

    fun onResumeWorkoutClicked() {
        // TODO: handle resuming
    }

    fun onExerciseProgressClicked(index: Int) {
        // TODO: navigate to exercise progress
    }

    fun onSetProgressClicked(index: Int) {
        // TODO: navigate to set progress
    }

    suspend fun getSessionProgress(workoutName: String): SessionProgressSnapshot? {
        val json = getSessionProgressUseCase(workoutName) ?: return null
        val map = Misc.readValue(json)
        return SessionProgressMapper.fromLegacyMap(map)
    }

    suspend fun saveSessionProgress(workoutName: String, snapshot: SessionProgressSnapshot) {
        val map = SessionProgressMapper.toLegacyMap(snapshot)
        val json = Misc.writeValueAsString(map)
        saveSessionProgressUseCase(workoutName, json)
    }

    fun finishWorkoutSession(workoutName: String, session: Session) {
        viewModelScope.launch {
            finishWorkoutSessionUseCase(workoutName, session)
        }
    }
}
