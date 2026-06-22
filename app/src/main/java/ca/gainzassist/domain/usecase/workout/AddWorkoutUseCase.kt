package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.repository.WorkoutRepository

class AddWorkoutUseCase(private val workoutRepository: WorkoutRepository) {

    suspend operator fun invoke(workout: Workout) {
        workoutRepository.insertWorkout(workout)
    }
}
