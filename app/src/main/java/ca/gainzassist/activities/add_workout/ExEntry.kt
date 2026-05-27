package ca.gainzassist.activities.add_workout

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import ca.gainzassist.R
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Exercise.SetsType.MAIN_SET
import java.util.*

class ExEntry : Fragment() {

    interface ExEntryDataListener {
        fun exerciseDoesNotExist(fmt: ExEntry, exerciseName: String, skipIndex: Int): Boolean
        fun exerciseDataReceived(exercise: Exercise, update: Boolean)
        fun deleteExercise(exercise: Exercise?, index: Int)
    }

    private var exEntryDataListener: ExEntryDataListener? = null

    var exercise: Exercise? = null
    var exerciseName: String? = null
    var num_reps = -1
    var num_sets = -1
    var ex_i = 0
    var minInt = 1
    var weight = -1f
    var minWeight = 0f
    var weightChange = 0f

    var deleteHidden = false

    private var isStateInitialized = false

    // Compose State
    private var uiState by mutableStateOf(ExEntryUiState(
        exerciseName = "",
        selectedEquipment = "Barbell",
        equipmentOptions = listOf("Barbell", "Dumbbell", "N/A"),
        weight = "45.0",
        reps = "10",
        sets = "3",
        showEnter = true,
        showUpdate = false,
        showDelete = true,
        duplicateExerciseError = null,
        canDecrementWeight = false,
        canDecrementReps = true,
        canDecrementSets = true,
        exerciseNameError = null,
        weightError = null,
        repsError = null,
        setsError = null
    ))

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExEntryDataListener) {
            exEntryDataListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement ExEntryDataListener")
        }
    }

    private fun initializeStateIfNeeded() {
        if (isStateInitialized) return

        num_reps = getString(R.string.starting_reps).toInt()
        num_sets = getString(R.string.starting_sets).toInt()

        val equipmentOptions = resources.getStringArray(R.array.exerciseEquipment).toList()

        // Initial setup for default equipment
        equipmentSelected(0)

        var initExerciseName = ""
        var initEquipment = equipmentOptions[0]
        var initShowEnter = true
        var initShowUpdate = false
        var initShowDelete = !deleteHidden

        if (exercise != null) {
            initExerciseName = exercise!!.name ?: ""
            weight = exercise!!.weight
            num_sets = exercise!!.sets
            num_reps = exercise!!.reps
            initEquipment = exercise!!.equipment ?: equipmentOptions[0]
            equipmentSelected(equipmentOptions.indexOf(initEquipment).takeIf { it >= 0 } ?: 0)

            initShowEnter = false
            initShowUpdate = true
        } else {
            weight = getString(R.string.starting_weight).toFloat()
        }

        uiState = uiState.copy(
            exerciseName = initExerciseName,
            selectedEquipment = initEquipment,
            equipmentOptions = equipmentOptions,
            weight = formatWeight(weight),
            reps = num_reps.toString(),
            sets = num_sets.toString(),
            showEnter = initShowEnter,
            showUpdate = initShowUpdate,
            showDelete = initShowDelete,
            canDecrementWeight = weight > minWeight,
            canDecrementReps = num_reps > minInt,
            canDecrementSets = num_sets > minInt
        )

        isStateInitialized = true
    }

    private fun formatWeight(value: Float): String {
        return if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
    }

    private fun applyExerciseToState(exercise: Exercise) {
        val newExerciseName = exercise.name ?: ""
        weight = exercise.weight
        num_sets = exercise.sets
        num_reps = exercise.reps
        val equipmentOptions = resources.getStringArray(R.array.exerciseEquipment).toList()
        val newEquipment = exercise.equipment ?: equipmentOptions[0]
        equipmentSelected(equipmentOptions.indexOf(newEquipment).takeIf { it >= 0 } ?: 0)

        uiState = uiState.copy(
            exerciseName = newExerciseName,
            selectedEquipment = newEquipment,
            weight = formatWeight(weight),
            reps = num_reps.toString(),
            sets = num_sets.toString(),
            showEnter = false,
            showUpdate = true,
            canDecrementWeight = weight > minWeight,
            canDecrementReps = num_reps > minInt,
            canDecrementSets = num_sets > minInt
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initializeStateIfNeeded()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ExEntryScreen(
                    uiState = uiState,
                    actions = object : ExEntryActions {
                        override fun onExerciseNameChanged(name: String) {
                            exerciseName = name
                            uiState = uiState.copy(
                                exerciseName = name,
                                duplicateExerciseError = null,
                                exerciseNameError = null
                            )
                        }

                        override fun onEquipmentSelected(equipment: String) {
                            uiState = uiState.copy(selectedEquipment = equipment)
                            equipmentSelected(uiState.equipmentOptions.indexOf(equipment))
                        }

                        override fun onWeightChanged(newWeight: String) {
                            uiState = uiState.copy(weight = newWeight, weightError = null)
                            val parsedWeight = newWeight.toFloatOrNull()
                            if (parsedWeight != null) {
                                weight = parsedWeight
                                uiState = uiState.copy(canDecrementWeight = weight > minWeight)
                            }
                        }

                        override fun onRepsChanged(newReps: String) {
                            uiState = uiState.copy(reps = newReps, repsError = null)
                            val parsedReps = newReps.toIntOrNull()
                            if (parsedReps != null) {
                                num_reps = parsedReps
                                uiState = uiState.copy(canDecrementReps = num_reps > minInt)
                            }
                        }

                        override fun onSetsChanged(newSets: String) {
                            uiState = uiState.copy(sets = newSets, setsError = null)
                            val parsedSets = newSets.toIntOrNull()
                            if (parsedSets != null) {
                                num_sets = parsedSets
                                uiState = uiState.copy(canDecrementSets = num_sets > minInt)
                            }
                        }

                        override fun onIncrementWeight() {
                            incNumWeight()
                        }

                        override fun onDecrementWeight() {
                            decNumWeight()
                        }

                        override fun onIncrementReps() {
                            incNumReps()
                        }

                        override fun onDecrementReps() {
                            decNumReps()
                        }

                        override fun onIncrementSets() {
                            incNumSets()
                        }

                        override fun onDecrementSets() {
                            decNumSets()
                        }

                        override fun onEnter() {
                            enterExercise()
                        }

                        override fun onUpdate() {
                            updateExercise()
                        }

                        override fun onDelete() {
                            deleteExercise()
                        }
                    }
                )
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        exEntryDataListener = null
    }

    fun setInd(index: Int) {
        this.ex_i = index
    }

    fun updateExFields(exercise: Exercise) {
        this.exercise = exercise
        if (isAdded && isStateInitialized) {
            applyExerciseToState(exercise)
        }
    }

    fun hideDelete() {
        deleteHidden = true
        uiState = uiState.copy(showDelete = false)
    }

    fun setExerciseExists() {
        uiState = uiState.copy(
            duplicateExerciseError = String.format(
                getString(R.string.err_exercise_exists),
                exerciseName
            )
        )
    }

    fun equipmentSelected(position: Int) {
        when (position) {
            0 -> {
                minWeight = ca.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
                weightChange = ca.gainzassist.constants.ExerciseConst.BB_WEIGHT_CHANGE
            }
            1 -> {
                minWeight = ca.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
                weightChange = ca.gainzassist.constants.ExerciseConst.DB_WEIGHT_CHANGE
            }
            else -> {
                minWeight = ca.gainzassist.constants.ExerciseConst.MIN_WEIGHT
                weightChange = ca.gainzassist.constants.ExerciseConst.WEIGHT_CHANGE
            }
        }

        if (weight <= minWeight) {
            weight = minWeight
        }
        uiState = uiState.copy(
            weight = formatWeight(weight),
            canDecrementWeight = weight > minWeight
        )
    }

    fun incNumWeight() {
        weight += weightChange
        uiState = uiState.copy(
            weight = formatWeight(weight),
            canDecrementWeight = weight > minWeight
        )
    }

    fun decNumWeight() {
        weight = Math.max(weight - weightChange, minWeight)
        uiState = uiState.copy(
            weight = formatWeight(weight),
            canDecrementWeight = weight > minWeight
        )
    }

    fun incNumReps() {
        num_reps += 1
        uiState = uiState.copy(
            reps = num_reps.toString(),
            canDecrementReps = num_reps > minInt
        )
    }

    fun decNumReps() {
        num_reps = Math.max(num_reps - 1, minInt)
        uiState = uiState.copy(
            reps = num_reps.toString(),
            canDecrementReps = num_reps > minInt
        )
    }

    fun incNumSets() {
        num_sets += 1
        uiState = uiState.copy(
            sets = num_sets.toString(),
            canDecrementSets = num_sets > minInt
        )
    }

    fun decNumSets() {
        num_sets = Math.max(num_sets - 1, minInt)
        uiState = uiState.copy(
            sets = num_sets.toString(),
            canDecrementSets = num_sets > minInt
        )
    }

    private fun validateComposeForm(): Boolean {
        var isValid = true
        var nameErr: String? = null
        var weightErr: String? = null
        var repsErr: String? = null
        var setsErr: String? = null

        if (uiState.exerciseName.trim().isEmpty()) {
            nameErr = getString(R.string.err_required)
            isValid = false
        }

        val parsedWeight = uiState.weight.toFloatOrNull()
        if (parsedWeight == null || uiState.weight.trim().isEmpty()) {
            weightErr = getString(R.string.err_required)
            isValid = false
        } else {
            weight = parsedWeight
        }

        val parsedReps = uiState.reps.toIntOrNull()
        if (parsedReps == null || uiState.reps.trim().isEmpty()) {
            repsErr = getString(R.string.err_required)
            isValid = false
        } else {
            num_reps = parsedReps
        }

        val parsedSets = uiState.sets.toIntOrNull()
        if (parsedSets == null || uiState.sets.trim().isEmpty()) {
            setsErr = getString(R.string.err_required)
            isValid = false
        } else {
            num_sets = parsedSets
        }

        uiState = uiState.copy(
            exerciseNameError = nameErr,
            weightError = weightErr,
            repsError = repsErr,
            setsError = setsErr
        )

        return isValid
    }

    fun enterExercise() {
        if (validateComposeForm()) {
            exerciseName = uiState.exerciseName
            if (exEntryDataListener!!.exerciseDoesNotExist(this, exerciseName!!, ex_i)) {
                uiState = uiState.copy(showEnter = false, showUpdate = true)
                exercise = Exercise(
                    ex_i,
                    exerciseName,
                    "Strength",
                    uiState.selectedEquipment,
                    num_sets,
                    num_reps,
                    weight,
                    MAIN_SET
                )
                exEntryDataListener!!.exerciseDataReceived(exercise!!, false)
            }
        }
    }

    fun updateExercise() {
        if (validateComposeForm()) {
            exerciseName = uiState.exerciseName
            if (exEntryDataListener!!.exerciseDoesNotExist(this, exerciseName!!, ex_i)) {
                exercise = Exercise(
                    ex_i,
                    exerciseName,
                    "Strength",
                    uiState.selectedEquipment,
                    num_sets,
                    num_reps,
                    weight,
                    MAIN_SET
                )
                exEntryDataListener!!.exerciseDataReceived(exercise!!, true)
            }
        }
    }

    fun deleteExercise() {
        uiState = uiState.copy(exerciseName = "")
        exEntryDataListener!!.deleteExercise(exercise, ex_i)
    }
}
