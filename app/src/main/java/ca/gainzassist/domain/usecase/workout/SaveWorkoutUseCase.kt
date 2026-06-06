package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.models.Workout

class SaveWorkoutUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout, isUpdate: Boolean) {
        if (isUpdate) {
            workoutRepository.updateWorkout(workout)
        } else {
            workoutRepository.insertWorkout(workout)
        }
    }
}
