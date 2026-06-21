package ca.gainzassist.feature.exercises_entry.presentation.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gainzassist.R
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.feature.exercises_entry.presentation.viewmodel.ExerciseEntryInputState
import ca.gainzassist.feature.exercises_entry.presentation.viewmodel.ExercisesEntryViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ExerciseEntryFragment : Fragment() {

    private val viewModel: ExercisesEntryViewModel by activityViewModel()

    private var exIndexState = mutableStateOf(0)
    var exIndex: Int
        get() = exIndexState.value
        set(value) {
            exIndexState.value = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val inputState = state.exerciseInputs.getOrNull(exIndex) ?: ExerciseEntryInputState()

                val equipmentOptions = resources.getStringArray(R.array.exerciseEquipment).toList()
                val uiState = ExEntryUiState(
                    exerciseName = inputState.exerciseName,
                    selectedEquipment = inputState.selectedEquipment,
                    equipmentOptions = equipmentOptions,
                    weight = inputState.weight,
                    reps = inputState.reps,
                    sets = inputState.sets,
                    showEnter = inputState.showEnter,
                    showUpdate = inputState.showUpdate,
                    showDelete = inputState.showDelete,
                    duplicateExerciseError = if (inputState.hasDuplicateError) {
                        String.format(getString(R.string.err_exercise_exists), inputState.exerciseName)
                    } else null,
                    canDecrementWeight = inputState.canDecrementWeight,
                    canDecrementReps = inputState.canDecrementReps,
                    canDecrementSets = inputState.canDecrementSets,
                    exerciseNameError = if (inputState.hasNameError) getString(R.string.err_required) else null,
                    weightError = if (inputState.hasWeightError) getString(R.string.err_required) else null,
                    repsError = if (inputState.hasRepsError) getString(R.string.err_required) else null,
                    setsError = if (inputState.hasSetsError) getString(R.string.err_required) else null
                )

                ExEntryScreen(
                    uiState = uiState,
                    actions = object : ExEntryActions {
                        override fun onExerciseNameChanged(name: String) {
                            viewModel.onExerciseNameChanged(exIndex, name)
                        }

                        override fun onEquipmentSelected(equipment: String) {
                            viewModel.onEquipmentSelected(exIndex, equipment)
                        }

                        override fun onWeightChanged(weight: String) {
                            viewModel.onWeightChanged(exIndex, weight)
                        }

                        override fun onRepsChanged(reps: String) {
                            viewModel.onRepsChanged(exIndex, reps)
                        }

                        override fun onSetsChanged(sets: String) {
                            viewModel.onSetsChanged(exIndex, sets)
                        }

                        override fun onIncrementWeight() {
                            viewModel.onIncrementWeight(exIndex)
                        }

                        override fun onDecrementWeight() {
                            viewModel.onDecrementWeight(exIndex)
                        }

                        override fun onIncrementReps() {
                            viewModel.onIncrementReps(exIndex)
                        }

                        override fun onDecrementReps() {
                            viewModel.onDecrementReps(exIndex)
                        }

                        override fun onIncrementSets() {
                            viewModel.onIncrementSets(exIndex)
                        }

                        override fun onDecrementSets() {
                            viewModel.onDecrementSets(exIndex)
                        }

                        override fun onEnter() {
                            viewModel.onExerciseSubmitted(exIndex)
                        }

                        override fun onUpdate() {
                            viewModel.onExerciseSubmitted(exIndex)
                        }

                        override fun onDelete() {
                            viewModel.onExerciseDeleted(exIndex)
                        }
                    }
                )
            }
        }
    }

    fun setInd(index: Int) {
        this.exIndex = index
    }

    fun updateExFields(exercise: Exercise) {
        viewModel.updateExFields(exIndex, exercise)
    }

    fun hideDelete() {
        // Transitional/Dumb: Managed by ViewModel state
    }

    fun showDelete() {
        // Transitional/Dumb: Managed by ViewModel state
    }

    fun setExerciseExists() {
        // Transitional/Dumb: Managed by ViewModel state
    }
}