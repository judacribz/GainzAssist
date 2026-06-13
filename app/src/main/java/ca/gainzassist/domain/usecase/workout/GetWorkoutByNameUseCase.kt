package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.repository.WorkoutRepository

class GetWorkoutByNameUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(name: String): Workout? {
        return workoutRepository.getWorkoutByName(name)
    }
}
