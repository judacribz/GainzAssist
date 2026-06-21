package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveUniqueExerciseNamesUseCase(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<String>> = workoutRepository.observeUniqueExerciseNames()
}
