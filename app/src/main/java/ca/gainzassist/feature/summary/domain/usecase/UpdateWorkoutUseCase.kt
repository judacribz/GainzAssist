package ca.gainzassist.feature.summary.domain.usecase

import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.repository.WorkoutRepository

class UpdateWorkoutUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout) {
        workoutRepository.updateWorkout(workout)
    }
}
