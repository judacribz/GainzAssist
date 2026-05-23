package ca.judacribz.gainzassist.activities.authentication.login

interface LoginActions {
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onToggleMode()
    fun onLoginClick()
    fun onSignUpClick()
    fun onGoogleSignInClick()
    fun onFacebookSignInClick()
    fun onImageBounceClick()
}
