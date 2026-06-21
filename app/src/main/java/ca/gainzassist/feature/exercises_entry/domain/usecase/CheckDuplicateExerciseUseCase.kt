package ca.gainzassist.feature.exercises_entry.domain.usecase

class CheckDuplicateExerciseUseCase {
    operator fun invoke(
        exerciseName: String,
        skipIndex: Int,
        existingExerciseNames: List<String>
    ): Boolean {
        val normalized = exerciseName.trim()
        if (normalized.isEmpty()) return false

        return existingExerciseNames.filterIndexed { index, _ -> index != skipIndex }
            .any { it.equals(normalized, ignoreCase = true) }
    }
}
