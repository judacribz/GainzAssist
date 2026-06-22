package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutsUseCase(private val workoutRepository: WorkoutRepository) {

    operator fun invoke(): Flow<List<Workout>> = workoutRepository.observeWorkouts()
}
