package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.repository.WorkoutRepository

class InsertCompletedSessionUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(session: Session, syncToFirebase: Boolean = true) {
        workoutRepository.insertCompletedSession(session, syncToFirebase)
    }
}
