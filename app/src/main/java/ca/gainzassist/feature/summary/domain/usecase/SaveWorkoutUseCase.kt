package ca.gainzassist.feature.summary.domain.usecase

import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.repository.WorkoutRepository

class SaveWorkoutUseCase(private val workoutRepository: WorkoutRepository) {

    suspend operator fun invoke(workout: Workout, isUpdate: Boolean) {
        if (isUpdate) {
            workoutRepository.updateWorkout(workout)
        } else {
            workoutRepository.insertWorkout(workout)
        }
    }
}
