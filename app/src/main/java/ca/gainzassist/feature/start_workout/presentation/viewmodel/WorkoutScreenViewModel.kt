package ca.gainzassist.feature.start_workout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.core.util.Misc
import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.session.SessionProgressMapper
import ca.gainzassist.domain.session.SessionProgressSnapshot
import ca.gainzassist.domain.usecase.session.GetSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.SaveSessionProgressUseCase
import ca.gainzassist.feature.start_workout.domain.usecase.FinishWorkoutSessionUseCase
import ca.gainzassist.feature.start_workout.presentation.event.WorkoutScreenEvent
import ca.gainzassist.feature.start_workout.presentation.state.WorkoutScreenState
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

    fun getSessionProgress(workoutName: String, onResult: (SessionProgressSnapshot?) -> Unit) {
        viewModelScope.launch {
            val json = getSessionProgressUseCase(workoutName)
            if (json == null) {
                onResult(null)
                return@launch
            }
            val map = Misc.readValue(json)
            onResult(SessionProgressMapper.fromLegacyMap(map))
        }
    }

    fun saveSessionProgress(workoutName: String, snapshot: SessionProgressSnapshot) {
        viewModelScope.launch {
            val map = SessionProgressMapper.toLegacyMap(snapshot)
            val json = Misc.writeValueAsString(map)
            saveSessionProgressUseCase(workoutName, json)
        }
    }

    fun finishWorkoutSession(workoutName: String, session: Session) {
        viewModelScope.launch {
            finishWorkoutSessionUseCase(workoutName, session)
        }
    }
}
