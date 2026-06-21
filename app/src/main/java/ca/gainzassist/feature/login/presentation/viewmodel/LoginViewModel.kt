package ca.gainzassist.feature.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.R
import ca.gainzassist.core.util.UiText
import ca.gainzassist.feature.login.domain.usecase.EmailErrorType
import ca.gainzassist.feature.login.domain.usecase.LoginValidationResult
import ca.gainzassist.feature.login.domain.usecase.LoginWithEmailUseCase
import ca.gainzassist.feature.login.domain.usecase.PasswordErrorType
import ca.gainzassist.feature.login.domain.usecase.ValidateLoginInputUseCase
import ca.gainzassist.feature.login.presentation.state.LoginUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LoginViewModelEvent {
    object NavigateToMain : LoginViewModelEvent
    data class ShowToast(val message: UiText) : LoginViewModelEvent
    object StartGoogleLogin : LoginViewModelEvent
    object StartFacebookLogin : LoginViewModelEvent
    data class AuthError(val message: UiText) : LoginViewModelEvent
}

class LoginViewModel(
    private val validateLoginInputUseCase: ValidateLoginInputUseCase,
    private val loginWithEmailUseCase: LoginWithEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginViewModelEvent>()
    val events: SharedFlow<LoginViewModelEvent> = _events.asSharedFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailErrorResId = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordErrorResId = null) }
    }

    fun onToggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode) }
    }

    fun onImageBounceClick() {
        _uiState.update { it.copy(imageBounceTrigger = it.imageBounceTrigger + 1) }
    }

    fun setFacebookEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isFacebookEnabled = enabled) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun onGoogleSignInClick() {
        viewModelScope.launch {
            _events.emit(LoginViewModelEvent.StartGoogleLogin)
        }
    }

    fun onFacebookSignInClick() {
        viewModelScope.launch {
            _events.emit(LoginViewModelEvent.StartFacebookLogin)
        }
    }

    fun onLoginClick() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        when (val validation = validateLoginInputUseCase(email, password)) {
            is LoginValidationResult.Success -> {
                _uiState.update { it.copy(isLoading = true) }
                viewModelScope.launch {
                    val result = loginWithEmailUseCase.login(email, password)
                    if (result.isSuccess) {
                        _events.emit(LoginViewModelEvent.NavigateToMain)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                        val errorMsg = result.exceptionOrNull()?.message ?: "Login failed"
                        _events.emit(
                            LoginViewModelEvent.AuthError(
                                if (result.exceptionOrNull()?.message != null) UiText.DynamicString(
                                    errorMsg
                                )
                                else UiText.StringResource(R.string.err_login_failed)
                            )
                        )
                    }
                }
            }

            is LoginValidationResult.Failure -> {
                val emailErrRes = when (validation.emailErrorType) {
                    EmailErrorType.EMPTY -> R.string.err_required
                    EmailErrorType.INVALID_FORMAT -> R.string.err_required_email_format
                    null -> null
                }
                val passwordErrRes = when (validation.passwordErrorType) {
                    PasswordErrorType.EMPTY -> R.string.err_required
                    PasswordErrorType.TOO_SHORT -> R.string.err_required_password_min
                    null -> null
                }
                _uiState.update {
                    it.copy(
                        emailErrorResId = emailErrRes,
                        passwordErrorResId = passwordErrRes
                    )
                }
            }
        }
    }

    fun onSignUpClick() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        when (val validation = validateLoginInputUseCase(email, password)) {
            is LoginValidationResult.Success -> {
                _uiState.update { it.copy(isLoading = true) }
                viewModelScope.launch {
                    val result = loginWithEmailUseCase.signUp(email, password)
                    if (result.isSuccess) {
                        _events.emit(LoginViewModelEvent.NavigateToMain)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                        val errorMsg = result.exceptionOrNull()?.message ?: "Sign up failed"
                        _events.emit(
                            LoginViewModelEvent.AuthError(
                                if (result.exceptionOrNull()?.message != null) UiText.DynamicString(
                                    errorMsg
                                )
                                else UiText.StringResource(R.string.err_signup_failed)
                            )
                        )
                    }
                }
            }

            is LoginValidationResult.Failure -> {
                val emailErrRes = when (validation.emailErrorType) {
                    EmailErrorType.EMPTY -> R.string.err_required
                    EmailErrorType.INVALID_FORMAT -> R.string.err_required_email_format
                    null -> null
                }
                val passwordErrRes = when (validation.passwordErrorType) {
                    PasswordErrorType.EMPTY -> R.string.err_required
                    PasswordErrorType.TOO_SHORT -> R.string.err_required_password_min
                    null -> null
                }
                _uiState.update {
                    it.copy(
                        emailErrorResId = emailErrRes,
                        passwordErrorResId = passwordErrRes
                    )
                }
            }
        }
    }
}
