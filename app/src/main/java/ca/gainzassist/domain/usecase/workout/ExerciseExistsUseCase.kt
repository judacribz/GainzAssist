package ca.gainzassist.domain.usecase.workout

class ExerciseExistsUseCase {

    operator fun invoke(
        existingExerciseNames: List<String>,
        exerciseName: String
    ): Boolean {
        val normalized = exerciseName.trim()
        if (normalized.isBlank()) return false

        return existingExerciseNames.any {
            it.equals(normalized, ignoreCase = true)
        }
    }
}
