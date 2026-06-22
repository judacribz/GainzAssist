package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository

class DeleteAllWorkoutsUseCase(private val workoutRepository: WorkoutRepository) {

    suspend operator fun invoke() {
        workoutRepository.deleteAllWorkouts()
    }
}
