package ca.gainzassist.domain.usecase.user

import ca.gainzassist.test.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserUseCaseTest {

    private lateinit var repository: FakeUserPreferencesRepository
    private lateinit var getUserEmailUseCase: GetUserEmailUseCase
    private lateinit var saveUserInfoUseCase: SaveUserInfoUseCase
    private lateinit var getThemeUseCase: GetThemeUseCase
    private lateinit var setThemeUseCase: SetThemeUseCase

    @Before
    fun setup() {
        repository = FakeUserPreferencesRepository()
        getUserEmailUseCase = GetUserEmailUseCase(repository)
        saveUserInfoUseCase = SaveUserInfoUseCase(repository)
        getThemeUseCase = GetThemeUseCase(repository)
        setThemeUseCase = SetThemeUseCase(repository)
    }

    @Test
    fun `user info use cases handle data correctly`() = runTest {
        assertEquals(null, getUserEmailUseCase())

        saveUserInfoUseCase("test@example.com", "uid123")
        assertEquals("test@example.com", getUserEmailUseCase())
    }

    @Test
    fun `theme use cases handle data correctly`() = runTest {
        assertEquals(null, getThemeUseCase())

        setThemeUseCase("Dark")
        assertEquals("Dark", getThemeUseCase())
        
        setThemeUseCase(null)
        assertEquals(null, getThemeUseCase())
    }
}
