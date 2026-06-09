package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.models.Workout

class GetWorkoutByNameUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(name: String): Workout? {
        return workoutRepository.getWorkoutByName(name)
    }
}
