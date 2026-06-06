package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.models.Workout
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutsUseCase(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<Workout>> {
        return workoutRepository.observeWorkouts()
    }
}
