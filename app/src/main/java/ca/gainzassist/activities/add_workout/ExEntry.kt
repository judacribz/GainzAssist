package ca.gainzassist.activities.add_workout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import ca.gainzassist.R
import ca.gainzassist.constants.ExerciseConst
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Exercise.SetsType.MAIN_SET
import java.util.Locale
import kotlin.math.max

class ExEntry : Fragment() {

    interface ExEntryDataListener {
        fun exerciseDoesNotExist(fmt: ExEntry, exerciseName: String?, skipIndex: Int): Boolean
        fun exerciseDataReceived(exercise: Exercise?, update: Boolean)
        fun deleteExercise(exercise: Exercise?, index: Int)
    }

    private var exEntryDataListener: ExEntryDataListener? = null

    var exercise: Exercise? = null
    var exerciseName: String? = null
    var numReps = -1
    var numSets = -1
    var exIndex = 0
    var minInt = 1
    var weight = -1f
    var minWeight = 0f
    var weightChange = 0f

    var deleteHidden = false

    private var isStateInitialized = false

    // Compose State
    private var uiState by mutableStateOf(
        ExEntryUiState(
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
        )
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExEntryDataListener) {
            exEntryDataListener = context
        } else {
            throw RuntimeException("$context must implement ExEntryDataListener")
        }
    }

    private fun initializeStateIfNeeded() {
        if (isStateInitialized) return

        numReps = getString(R.string.starting_reps).toInt()
        numSets = getString(R.string.starting_sets).toInt()

        val equipmentOptions = resources.getStringArray(R.array.exerciseEquipment).toList()

        // Initial setup for default equipment
        equipmentSelected(0)

        var initExerciseName = ""
        var initEquipment = equipmentOptions[0]
        var initShowEnter = true
        var initShowUpdate = false

        if (exercise != null) {
            initExerciseName = exercise?.name.orEmpty()
            weight = exercise?.weight ?: 0f
            numSets = exercise?.sets ?: 0
            numReps = exercise?.reps ?: 0
            initEquipment = exercise?.equipment ?: equipmentOptions[0]
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
            reps = numReps.toString(),
            sets = numSets.toString(),
            showEnter = initShowEnter,
            showUpdate = initShowUpdate,
            showDelete = !deleteHidden,
            canDecrementWeight = weight > minWeight,
            canDecrementReps = numReps > minInt,
            canDecrementSets = numSets > minInt
        )

        isStateInitialized = true
    }

    private fun formatWeight(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f", value)
    }

    private fun applyExerciseToState(exercise: Exercise) {
        val newExerciseName = exercise.name ?: ""
        weight = exercise.weight
        numSets = exercise.sets
        numReps = exercise.reps

        val equipmentOptions = context
            ?.resources
            ?.getStringArray(R.array.exerciseEquipment)
            ?.toList()
            ?: uiState.equipmentOptions

        val fallbackEquipment = equipmentOptions.firstOrNull() ?: "Barbell"
        val newEquipment = exercise.equipment ?: fallbackEquipment
        equipmentSelected(equipmentOptions.indexOf(newEquipment).takeIf { it >= 0 } ?: 0)

        uiState = uiState.copy(
            exerciseName = newExerciseName,
            selectedEquipment = newEquipment,
            equipmentOptions = equipmentOptions,
            weight = formatWeight(weight),
            reps = numReps.toString(),
            sets = numSets.toString(),
            showEnter = false,
            showUpdate = true,
            canDecrementWeight = weight > minWeight,
            canDecrementReps = numReps > minInt,
            canDecrementSets = numSets > minInt
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

                        override fun onWeightChanged(weight: String) {
                            uiState = uiState.copy(weight = weight, weightError = null)
                            val parsedWeight = weight.toFloatOrNull()
                            if (parsedWeight != null) {
                                this@ExEntry.weight = parsedWeight
                                uiState =
                                    uiState.copy(canDecrementWeight = this@ExEntry.weight > minWeight)
                            }
                        }

                        override fun onRepsChanged(reps: String) {
                            uiState = uiState.copy(reps = reps, repsError = null)
                            val parsedReps = reps.toIntOrNull()
                            if (parsedReps != null) {
                                numReps = parsedReps
                                uiState = uiState.copy(canDecrementReps = numReps > minInt)
                            }
                        }

                        override fun onSetsChanged(sets: String) {
                            uiState = uiState.copy(sets = sets, setsError = null)
                            val parsedSets = sets.toIntOrNull()
                            if (parsedSets != null) {
                                numSets = parsedSets
                                uiState = uiState.copy(canDecrementSets = numSets > minInt)
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
        this.exIndex = index
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
                minWeight = ExerciseConst.BB_MIN_WEIGHT
                weightChange = ExerciseConst.BB_WEIGHT_CHANGE
            }

            1 -> {
                minWeight = ExerciseConst.DB_MIN_WEIGHT
                weightChange = ExerciseConst.DB_WEIGHT_CHANGE
            }

            else -> {
                minWeight = ExerciseConst.MIN_WEIGHT
                weightChange = ExerciseConst.WEIGHT_CHANGE
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
        weight = max(weight - weightChange, minWeight)
        uiState = uiState.copy(
            weight = formatWeight(weight),
            canDecrementWeight = weight > minWeight
        )
    }

    fun incNumReps() {
        numReps += 1
        uiState = uiState.copy(
            reps = numReps.toString(),
            canDecrementReps = numReps > minInt
        )
    }

    fun decNumReps() {
        numReps = max(numReps - 1, minInt)
        uiState = uiState.copy(
            reps = numReps.toString(),
            canDecrementReps = numReps > minInt
        )
    }

    fun incNumSets() {
        numSets += 1
        uiState = uiState.copy(
            sets = numSets.toString(),
            canDecrementSets = numSets > minInt
        )
    }

    fun decNumSets() {
        numSets = max(numSets - 1, minInt)
        uiState = uiState.copy(
            sets = numSets.toString(),
            canDecrementSets = numSets > minInt
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
            weight = max(parsedWeight, minWeight)
        }

        val parsedReps = uiState.reps.toIntOrNull()
        if (parsedReps == null || uiState.reps.trim().isEmpty()) {
            repsErr = getString(R.string.err_required)
            isValid = false
        } else {
            numReps = max(parsedReps, minInt)
        }

        val parsedSets = uiState.sets.toIntOrNull()
        if (parsedSets == null || uiState.sets.trim().isEmpty()) {
            setsErr = getString(R.string.err_required)
            isValid = false
        } else {
            numSets = max(parsedSets, minInt)
        }

        uiState = uiState.copy(
            exerciseNameError = nameErr,
            weightError = weightErr,
            repsError = repsErr,
            setsError = setsErr,
            weight = formatWeight(weight),
            reps = numReps.toString(),
            sets = numSets.toString()
        )

        return isValid
    }

    fun enterExercise() {
        if (!validateComposeForm()) return

        val listener = exEntryDataListener ?: return
        val name = uiState.exerciseName.trim().takeIf { it.isNotEmpty() } ?: run {
            uiState = uiState.copy(exerciseNameError = getString(R.string.err_required))
            return
        }

        if (!listener.exerciseDoesNotExist(this, name, exIndex)) return

        val newExercise = Exercise(
            exIndex,
            name,
            "Strength",
            uiState.selectedEquipment,
            numSets,
            numReps,
            weight,
            MAIN_SET
        )

        exerciseName = name
        exercise = newExercise
        uiState = uiState.copy(showEnter = false, showUpdate = true)

        listener.exerciseDataReceived(newExercise, false)
    }

    fun updateExercise() {
        if (!validateComposeForm()) return

        val listener = exEntryDataListener ?: return
        val name = uiState.exerciseName.trim().takeIf { it.isNotEmpty() } ?: run {
            uiState = uiState.copy(exerciseNameError = getString(R.string.err_required))
            return
        }

        if (!listener.exerciseDoesNotExist(this, name, exIndex)) return

        val updatedExercise = Exercise(
            exIndex,
            name,
            "Strength",
            uiState.selectedEquipment,
            numSets,
            numReps,
            weight,
            MAIN_SET
        )

        exerciseName = name
        exercise = updatedExercise

        listener.exerciseDataReceived(updatedExercise, true)
    }

    fun deleteExercise() {
        uiState = uiState.copy(exerciseName = "")
        exEntryDataListener?.deleteExercise(exercise, exIndex)
    }
}
