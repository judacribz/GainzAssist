package ca.gainzassist.feature.exercises_entry.domain.usecase

enum class ValidationError {
    REQUIRED
}

sealed interface ValidateExerciseResult {
    data class Success(val name: String, val weight: Float, val reps: Int, val sets: Int) : ValidateExerciseResult

    data class Error(
        val nameError: ValidationError? = null,
        val weightError: ValidationError? = null,
        val repsError: ValidationError? = null,
        val setsError: ValidationError? = null
    ) : ValidateExerciseResult
}

class ValidateExerciseInputUseCase {

    operator fun invoke(
        name: String,
        weightStr: String,
        repsStr: String,
        setsStr: String,
        minWeight: Float,
        minInt: Int = 1
    ): ValidateExerciseResult {
        var isValid = true
        var nameErr: ValidationError? = null
        var weightErr: ValidationError? = null
        var repsErr: ValidationError? = null
        var setsErr: ValidationError? = null

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            nameErr = ValidationError.REQUIRED
            isValid = false
        }

        val parsedWeight = weightStr.toFloatOrNull()
        if (parsedWeight == null || weightStr.trim().isEmpty()) {
            weightErr = ValidationError.REQUIRED
            isValid = false
        }

        val parsedReps = repsStr.toIntOrNull()
        if (parsedReps == null || repsStr.trim().isEmpty()) {
            repsErr = ValidationError.REQUIRED
            isValid = false
        }

        val parsedSets = setsStr.toIntOrNull()
        if (parsedSets == null || setsStr.trim().isEmpty()) {
            setsErr = ValidationError.REQUIRED
            isValid = false
        }

        return if (isValid && parsedWeight != null && parsedReps != null && parsedSets != null) {
            ValidateExerciseResult.Success(
                name = trimmedName,
                weight = maxOf(parsedWeight, minWeight),
                reps = maxOf(parsedReps, minInt),
                sets = maxOf(parsedSets, minInt)
            )
        } else {
            ValidateExerciseResult.Error(
                nameError = nameErr,
                weightError = weightErr,
                repsError = repsErr,
                setsError = setsErr
            )
        }
    }
}
