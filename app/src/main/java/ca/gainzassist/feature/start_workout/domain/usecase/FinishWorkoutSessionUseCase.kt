package ca.gainzassist.feature.start_workout.domain.usecase

import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.usecase.session.RemoveIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteWorkoutUseCase
import ca.gainzassist.domain.usecase.session.RemoveSessionProgressUseCase
import ca.gainzassist.domain.usecase.workout.InsertCompletedSessionUseCase

class FinishWorkoutSessionUseCase(
    private val insertCompletedSessionUseCase: InsertCompletedSessionUseCase,
    private val removeIncompleteWorkoutUseCase: RemoveIncompleteWorkoutUseCase,
    private val removeIncompleteSessionUseCase: RemoveIncompleteSessionUseCase,
    private val removeSessionProgressUseCase: RemoveSessionProgressUseCase
) {
    suspend operator fun invoke(workoutName: String, session: Session) {
        insertCompletedSessionUseCase(session, syncToFirebase = true)
        
        if (workoutName.isNotBlank()) {
            if (removeIncompleteWorkoutUseCase(workoutName)) {
                removeIncompleteSessionUseCase(workoutName)
            }
            removeSessionProgressUseCase(workoutName)
        }
    }
}
