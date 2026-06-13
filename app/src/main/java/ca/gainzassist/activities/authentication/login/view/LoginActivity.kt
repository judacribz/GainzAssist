package ca.gainzassist.activities.authentication.login.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.gainzassist.BuildConfig
import ca.gainzassist.R
import ca.gainzassist.activities.authentication.login.view.LoginActions
import ca.gainzassist.activities.authentication.login.view.LoginUiState
import ca.gainzassist.activities.main.view.MainActivity
import ca.gainzassist.core.util.UI
import ca.gainzassist.data.local.preferences.Preferences
import ca.gainzassist.data.remote.firebase.Authentication
import ca.gainzassist.data.remote.firebase.Database
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.orhanobut.logger.Logger
import java.io.IOException

import androidx.activity.result.contract.ActivityResultContracts
class LoginActivity : AppCompatActivity(), FacebookCallback<LoginResult>, FirebaseAuth.AuthStateListener {

    companion object {
        private const val MIN_PASSWORD_LEN = 6
        private const val LOGIN_IMG = "squat.png"
        private const val SIGN_UP_IMG = "fatman.png"
    }

    private var auth: FirebaseAuth? = null
    private var credential: AuthCredential? = null
    private var googleCred: AuthCredential? = null
    private var signInClient: GoogleSignInClient? = null
    private var callbackManager: CallbackManager? = null

    var linkGoogle = false

    // State for Compose
    private var uiState by mutableStateOf(LoginUiState())

    private val isFacebookEnabled: Boolean
        get() = BuildConfig.ENABLE_FACEBOOK_LOGIN.toBooleanStrictOrNull() ?: false

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            uiState = uiState.copy(isLoading = true)
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val token = account?.idToken
                if (token != null) {
                    googleCred = GoogleAuthProvider.getCredential(token, null)
                    val cred = googleCred
                    if (cred != null) {
                        Authentication.signIn(this, cred)
                    } else {
                        authError("Google authentication failed: credential null")
                    }
                } else {
                    authError("Google authentication failed: account or ID token null")
                }
            } catch (ex: ApiException) {
                ex.printStackTrace()
                uiState = uiState.copy(isLoading = false)
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            uiState = uiState.copy(isLoading = false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UI.setInitTheme(this)

        uiState = uiState.copy(isFacebookEnabled = isFacebookEnabled)

        setupSignInMethods()

        val loginBitmap = loadBitmapFromAssets(LOGIN_IMG)
        val signUpBitmap = loadBitmapFromAssets(SIGN_UP_IMG)

        setContent {
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

                    override fun onLoginClick() {
                        login()
                    }

                    override fun onSignUpClick() {
                        signUp()
                    }

                    override fun onGoogleSignInClick() {
                        googleLogin()
                    }

                    override fun onFacebookSignInClick() {
                        facebookLogin()
                    }

                    override fun onImageBounceClick() {
                        uiState = uiState.copy(imageBounceTrigger = uiState.imageBounceTrigger + 1)
                    }
                }
            )
        }
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, gso)

        if (isFacebookEnabled) {
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager, this)
        }
    }

    override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(MainActivity.EXTRA_LOGOUT_USER, false)) {
            signInClient?.let { Authentication.signOut(this, it) }
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
        val client = signInClient
        val signInIntent = client?.signInIntent
        if (client != null && signInIntent != null) {
            googleSignInLauncher.launch(signInIntent)
        } else {
            authError("Google Login unavailable: client uninitialized")
        }
    }

    fun facebookLogin() {
        if (isFacebookEnabled) {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
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