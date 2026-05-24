package ca.gainzassist.activities.authentication.login

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoginMode: Boolean = true,
    val isFacebookEnabled: Boolean = true,
    val imageBounceTrigger: Int = 0,
    val isLoading: Boolean = false
)
