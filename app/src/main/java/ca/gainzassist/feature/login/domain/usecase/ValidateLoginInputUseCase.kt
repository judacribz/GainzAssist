package ca.gainzassist.feature.login.domain.usecase

sealed interface LoginValidationResult {
    object Success : LoginValidationResult
    data class Failure(
        val emailErrorType: EmailErrorType?,
        val passwordErrorType: PasswordErrorType?
    ) : LoginValidationResult
}

enum class EmailErrorType {

    EMPTY,
    INVALID_FORMAT
}

enum class PasswordErrorType {
    EMPTY,
    TOO_SHORT
}

class ValidateLoginInputUseCase {

    operator fun invoke(email: String, password: String): LoginValidationResult {
        var emailError: EmailErrorType? = null
        var passwordError: PasswordErrorType? = null

        if (email.isEmpty()) {
            emailError = EmailErrorType.EMPTY
        } else if (!EMAIL_REGEX.matches(email)) {
            emailError = EmailErrorType.INVALID_FORMAT
        }

        if (password.isEmpty()) {
            passwordError = PasswordErrorType.EMPTY
        } else if (password.length < 6) {
            passwordError = PasswordErrorType.TOO_SHORT
        }

        return if (emailError == null && passwordError == null) {
            LoginValidationResult.Success
        } else {
            LoginValidationResult.Failure(emailError, passwordError)
        }
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    }
}
