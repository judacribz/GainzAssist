package ca.gainzassist.feature.exercises_entry.domain.usecase

import ca.gainzassist.domain.model.Exercise

import ca.gainzassist.core.constants.ExerciseConst

class BuildExerciseUseCase {
    operator fun invoke(
        index: Int,
        name: String,
        equipment: String,
        sets: Int,
        reps: Int,
        weight: Float
    ): Exercise {
        return Exercise(
            index,
            name,
            ExerciseConst.STRENGTH,
            equipment,
            sets,
            reps,
            weight,
            Exercise.SetsType.MAIN_SET
        )
    }
}
