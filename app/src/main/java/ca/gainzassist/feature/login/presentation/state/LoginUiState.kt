package ca.gainzassist.feature.login.presentation.state

data class LoginUiState(
    val email: String = "",
    val emailErrorResId: Int? = null,
    val password: String = "",
    val passwordErrorResId: Int? = null,
    val isLoginMode: Boolean = true,
    val isFacebookEnabled: Boolean = true,
    val imageBounceTrigger: Int = 0,
    val isLoading: Boolean = false
)
