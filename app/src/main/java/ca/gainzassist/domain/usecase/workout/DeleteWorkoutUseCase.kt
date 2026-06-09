package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository

class DeleteWorkoutUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(workoutName: String) {
        workoutRepository.deleteWorkoutByName(workoutName)
    }
}
