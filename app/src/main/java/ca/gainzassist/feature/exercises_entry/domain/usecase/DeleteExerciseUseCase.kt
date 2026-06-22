package ca.gainzassist.feature.exercises_entry.domain.usecase

import ca.gainzassist.domain.model.Exercise

class DeleteExerciseUseCase {

    operator fun invoke(
        exercises: List<Exercise>,
        index: Int
    ): List<Exercise> {
        if (index < 0 || index >= exercises.size) return exercises

        val list = exercises.toMutableList()
        list.removeAt(index)

        // Re-index remaining exercises
        return list.mapIndexed { i, ex ->
            ex.apply { exerciseNumber = i }
        }
    }
}
