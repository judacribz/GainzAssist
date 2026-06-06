package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.models.Workout

class UpdateWorkoutUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout) {
        workoutRepository.updateWorkout(workout)
    }
}
