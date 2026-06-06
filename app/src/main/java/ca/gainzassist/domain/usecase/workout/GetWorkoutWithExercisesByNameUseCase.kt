package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.models.Workout

class GetWorkoutWithExercisesByNameUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(name: String): Workout? {
        return workoutRepository.getWorkoutWithExercisesByName(name)
    }
}
