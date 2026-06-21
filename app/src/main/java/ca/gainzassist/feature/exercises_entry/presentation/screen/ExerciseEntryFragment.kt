package ca.gainzassist.feature.exercises_entry.presentation.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gainzassist.R
import ca.gainzassist.feature.exercises_entry.presentation.viewmodel.ExerciseEntryInputState
import ca.gainzassist.feature.exercises_entry.presentation.viewmodel.ExercisesEntryViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ExerciseEntryFragment : Fragment() {

    private val viewModel: ExercisesEntryViewModel by activityViewModel()

    private val exerciseIndex: Int
        get() = requireArguments().getInt(ARG_EXERCISE_INDEX)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val inputState = state.exerciseInputs.getOrNull(exerciseIndex) ?: ExerciseEntryInputState()

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
                } else {
                    null
                },
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
                        viewModel.onExerciseNameChanged(exerciseIndex, name)
                    }

                    override fun onEquipmentSelected(equipment: String) {
                        viewModel.onEquipmentSelected(exerciseIndex, equipment)
                    }

                    override fun onWeightChanged(weight: String) {
                        viewModel.onWeightChanged(exerciseIndex, weight)
                    }

                    override fun onRepsChanged(reps: String) {
                        viewModel.onRepsChanged(exerciseIndex, reps)
                    }

                    override fun onSetsChanged(sets: String) {
                        viewModel.onSetsChanged(exerciseIndex, sets)
                    }

                    override fun onIncrementWeight() {
                        viewModel.onIncrementWeight(exerciseIndex)
                    }

                    override fun onDecrementWeight() {
                        viewModel.onDecrementWeight(exerciseIndex)
                    }

                    override fun onIncrementReps() {
                        viewModel.onIncrementReps(exerciseIndex)
                    }

                    override fun onDecrementReps() {
                        viewModel.onDecrementReps(exerciseIndex)
                    }

                    override fun onIncrementSets() {
                        viewModel.onIncrementSets(exerciseIndex)
                    }

                    override fun onDecrementSets() {
                        viewModel.onDecrementSets(exerciseIndex)
                    }

                    override fun onEnter() {
                        viewModel.onExerciseSubmitted(exerciseIndex)
                    }

                    override fun onUpdate() {
                        viewModel.onExerciseSubmitted(exerciseIndex)
                    }

                    override fun onDelete() {
                        viewModel.onExerciseDeleted(exerciseIndex)
                    }
                }
            )
        }
    }

    companion object {
        private const val ARG_EXERCISE_INDEX = "exercise_index"

        fun newInstance(index: Int): ExerciseEntryFragment = ExerciseEntryFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_EXERCISE_INDEX, index)
            }
        }
    }
}
