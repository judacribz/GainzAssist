package ca.gainzassist.feature.workout_entry.domain.usecase

import ca.gainzassist.core.constants.ExerciseConst

sealed interface WorkoutEntryValidationResult {
    object Success : WorkoutEntryValidationResult
    object InvalidNumber : WorkoutEntryValidationResult
}

class ValidateWorkoutEntryUseCase {

    operator fun invoke(numberOfExercises: String): WorkoutEntryValidationResult {
        val numEx = numberOfExercises.toIntOrNull()
        return if (numEx == null || numEx < ExerciseConst.MIN_INT) {
            WorkoutEntryValidationResult.InvalidNumber
        } else {
            WorkoutEntryValidationResult.Success
        }
    }
}
