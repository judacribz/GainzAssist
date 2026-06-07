package ca.gainzassist.presentation.start_workout.workout

import androidx.lifecycle.ViewModel
import ca.gainzassist.domain.session.SessionProgressMapper
import ca.gainzassist.domain.session.SessionProgressSnapshot
import ca.gainzassist.domain.usecase.session.GetSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.SaveSessionProgressUseCase
import ca.gainzassist.util.Misc
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkoutScreenViewModel(
    private val getSessionProgressUseCase: GetSessionProgressUseCase,
    private val saveSessionProgressUseCase: SaveSessionProgressUseCase
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
        @Suppress("UNCHECKED_CAST")
        val map = Misc.readValue(json) as? Map<String, Any>
        return SessionProgressMapper.fromLegacyMap(map)
    }

    suspend fun saveSessionProgress(workoutName: String, snapshot: SessionProgressSnapshot) {
        val map = SessionProgressMapper.toLegacyMap(snapshot)
        val json = Misc.writeValueAsString(map)
        saveSessionProgressUseCase(workoutName, json)
    }
}
