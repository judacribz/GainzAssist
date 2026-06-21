package ca.gainzassist.feature.exercises_entry.domain.usecase

import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout

class BuildWorkoutFromExerciseEntriesUseCase {
    operator fun invoke(
        workoutName: String,
        exercises: List<Exercise>
    ): Workout = Workout().apply {
        this.id = -1
        this.name = workoutName
        this.exercises = ArrayList(exercises)
    }
}
