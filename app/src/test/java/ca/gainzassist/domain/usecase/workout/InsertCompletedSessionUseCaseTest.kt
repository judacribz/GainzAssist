package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.domain.model.Session
import ca.gainzassist.test.fakes.FakeWorkoutRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsertCompletedSessionUseCaseTest {

    @Test
    fun `invoke delegates to repository`() = runBlocking {
        val repository = FakeWorkoutRepository()
        val useCase = InsertCompletedSessionUseCase(repository)

        val session = Session().apply {
            workoutId = 1L
            workoutName = "Test Workout"
        }

        useCase(session, syncToFirebase = false)

        assertEquals(1, repository.insertedSessions.size)
        assertTrue(repository.insertedSessions.contains(session))
    }
}
