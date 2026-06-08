package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.models.Session

class InsertCompletedSessionUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(session: Session, syncToFirebase: Boolean = true) {
        workoutRepository.insertCompletedSession(session, syncToFirebase)
    }
}
