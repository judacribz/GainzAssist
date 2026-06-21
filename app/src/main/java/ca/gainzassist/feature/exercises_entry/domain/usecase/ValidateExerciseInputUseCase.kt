package ca.gainzassist.feature.exercises_entry.domain.usecase

sealed interface ExerciseInputValidationResult {
    object Success : ExerciseInputValidationResult
    data class Failure(
        val isNameBlank: Boolean,
        val isWeightInvalid: Boolean,
        val isRepsInvalid: Boolean,
        val isSetsInvalid: Boolean
    ) : ExerciseInputValidationResult
}

class ValidateExerciseInputUseCase {
    operator fun invoke(
        name: String,
        weightStr: String,
        repsStr: String,
        setsStr: String
    ): ExerciseInputValidationResult {
        val isNameBlank = name.trim().isEmpty()
        val weight = weightStr.toFloatOrNull()
        val isWeightInvalid = weight == null || weightStr.trim().isEmpty()
        val reps = repsStr.toIntOrNull()
        val isRepsInvalid = reps == null || repsStr.trim().isEmpty()
        val sets = setsStr.toIntOrNull()
        val isSetsInvalid = sets == null || setsStr.trim().isEmpty()

        return if (!isNameBlank && !isWeightInvalid && !isRepsInvalid && !isSetsInvalid) {
            ExerciseInputValidationResult.Success
        } else {
            ExerciseInputValidationResult.Failure(isNameBlank, isWeightInvalid, isRepsInvalid, isSetsInvalid)
        }
    }
}
