package ca.gainzassist.activities.authentication.login.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import ca.gainzassist.BuildConfig
import ca.gainzassist.R
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.activities.main.view.MainActivity
import ca.gainzassist.data.local.preferences.Preferences
import ca.gainzassist.data.remote.firebase.Authentication
import ca.gainzassist.data.remote.firebase.Database
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

class LoginActivity : GainzBaseActivity(), FacebookCallback<LoginResult>,
    FirebaseAuth.AuthStateListener {

    companion object {
        private const val MIN_PASSWORD_LEN = 6
        private const val LOGIN_IMG = "squat.png"
        private const val SIGN_UP_IMG = "fatman.png"
    }

    private var auth: FirebaseAuth? = null
    private var credential: AuthCredential? = null
    private var googleCred: AuthCredential? = null
    private var callbackManager: CallbackManager? = null
    private var loginBitmap: Bitmap? = null
    private var signUpBitmap: Bitmap? = null

    var linkGoogle = false

    // State for Compose
    private var uiState by mutableStateOf(LoginUiState())

    private val isFacebookEnabled: Boolean
        get() = BuildConfig.ENABLE_FACEBOOK_LOGIN.toBooleanStrictOrNull() ?: false

    override fun onBeforeSetContent() {
        uiState = uiState.copy(isFacebookEnabled = isFacebookEnabled)
        setupSignInMethods()
        loginBitmap = loadBitmapFromAssets(LOGIN_IMG)
        signUpBitmap = loadBitmapFromAssets(SIGN_UP_IMG)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun InnerContent() {
        LoginScreen(
            state = uiState,
            loginImage = loginBitmap,
            signUpImage = signUpBitmap,
            actions = object : LoginActions {
                override fun onEmailChanged(email: String) {
                    uiState = uiState.copy(email = email, emailError = null)
                }

                override fun onPasswordChanged(password: String) {
                    uiState = uiState.copy(password = password, passwordError = null)
                }

                override fun onToggleMode() {
                    uiState = uiState.copy(isLoginMode = !uiState.isLoginMode)
                }

                override fun onLoginClick() = login()

                override fun onSignUpClick() = signUp()

                override fun onGoogleSignInClick() = googleLogin()

                override fun onFacebookSignInClick() = facebookLogin()

                override fun onImageBounceClick() {
                    uiState = uiState.copy(imageBounceTrigger = uiState.imageBounceTrigger + 1)
                }
            }
        )
    }

    private fun loadBitmapFromAssets(fileName: String): Bitmap? {
        return try {
            BitmapFactory.decodeStream(assets.open(fileName))
        } catch (ioe: IOException) {
            ioe.printStackTrace()
            null
        }
    }

    private fun setupSignInMethods() {
        auth = FirebaseAuth.getInstance()

        if (isFacebookEnabled) {
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager, this)
        }
    }

    override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(MainActivity.EXTRA_LOGOUT_USER, false)) {
            Authentication.signOut(this)
            lifecycleScope.launch {
                try {
                    CredentialManager
                        .create(this@LoginActivity)
                        .clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (isFacebookEnabled) {
                LoginManager.getInstance().logOut()
            }
        }
        auth?.addAuthStateListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        super.onStop()
        auth?.removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            uiState = uiState.copy(isLoading = true)
            Toast.makeText(
                this,
                String.format(getString(R.string.txt_logged_in), firebaseUser.email),
                Toast.LENGTH_SHORT
            ).show()
            if (linkGoogle) {
                credential?.let { Authentication.linkUser(this, it, firebaseUser) }
            }
            Preferences.setUserInfoPref(this, firebaseUser.email, firebaseUser.uid)
            Database.setUserInfo(this)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onSuccess(result: LoginResult) {
        credential = FacebookAuthProvider.getCredential(result.accessToken.token)
        val cred = credential
        if (cred != null) {
            Authentication.signIn(this, cred)
        } else {
            authError("Facebook authentication failed: credential null")
        }
    }

    override fun onCancel() {
        uiState = uiState.copy(isLoading = false)
    }

    override fun onError(error: FacebookException) {
        error.printStackTrace()
        uiState = uiState.copy(isLoading = false)
        Toast.makeText(this, "Facebook Login failed", Toast.LENGTH_SHORT).show()
    }

    private fun authError(message: String) {
        Logger.e(message)
        uiState = uiState.copy(isLoading = false)
        Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
    }

    fun validateForm(email: String, password: String): Boolean {
        var emailError: String? = null
        var passwordError: String? = null
        var isValid = true

        if (email.isEmpty()) {
            emailError = getString(R.string.err_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = getString(R.string.err_required_email_format)
            isValid = false
        }

        if (password.isEmpty()) {
            passwordError = getString(R.string.err_required)
            isValid = false
        } else if (password.length < MIN_PASSWORD_LEN) {
            passwordError = getString(R.string.err_required_password_min)
            isValid = false
        }

        uiState = uiState.copy(emailError = emailError, passwordError = passwordError)
        return isValid
    }

    fun googleLogin() {
        uiState = uiState.copy(isLoading = true)
        val credentialManager = CredentialManager.create(this)
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.joinToString("") { "%02x".format(it) }
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setNonce(hashedNonce)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val token = googleIdTokenCredential.idToken
                    googleCred = GoogleAuthProvider.getCredential(token, null)
                    val cred = googleCred
                    if (cred != null) {
                        Authentication.signIn(this@LoginActivity, cred)
                    } else {
                        authError("Google authentication failed: credential null")
                    }
                } else {
                    authError("Google authentication failed: Unexpected credential type")
                }
            } catch (e: GetCredentialException) {
                e.printStackTrace()
                uiState = uiState.copy(isLoading = false)
                Toast.makeText(this@LoginActivity, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            } catch (e: GoogleIdTokenParsingException) {
                e.printStackTrace()
                authError("Google authentication failed: Parsing exception")
            }
        }
    }

    fun facebookLogin() {
        if (isFacebookEnabled) {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("public_profile", "email"))
        }
    }

    fun login() {
        val email = uiState.email.trim { it <= ' ' }
        val password = uiState.password.trim { it <= ' ' }
        if (validateForm(email, password)) {
            uiState = uiState.copy(isLoading = true)
            credential = EmailAuthProvider.getCredential(email, password)
            val cred = credential
            if (cred != null) {
                Authentication.signIn(this, cred)
            } else {
                authError("Email Login failed: credential null")
            }
        }
    }

    fun signUp() {
        val email = uiState.email.trim { it <= ' ' }
        val password = uiState.password.trim { it <= ' ' }
        if (validateForm(email, password)) {
            uiState = uiState.copy(isLoading = true)
            credential = EmailAuthProvider.getCredential(email, password)
            Authentication.createUser(this, email, password)
        }
    }

    fun loginFail() {
        uiState = uiState.copy(isLoading = false)
    }
}