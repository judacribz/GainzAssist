package ca.judacribz.gainzassist.activities.authentication

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
import ca.judacribz.gainzassist.BuildConfig
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.authentication.login.LoginActions
import ca.judacribz.gainzassist.activities.authentication.login.LoginScreen
import ca.judacribz.gainzassist.activities.authentication.login.LoginUiState
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.activities.main.Main.Companion.EXTRA_LOGOUT_USER
import ca.judacribz.gainzassist.util.Preferences.setUserInfoPref
import ca.judacribz.gainzassist.util.UI.setInitTheme
import ca.judacribz.gainzassist.util.firebase.Authentication.RC_SIGN_IN
import ca.judacribz.gainzassist.util.firebase.Authentication.createUser
import ca.judacribz.gainzassist.util.firebase.Authentication.linkUser
import ca.judacribz.gainzassist.util.firebase.Authentication.signIn
import ca.judacribz.gainzassist.util.firebase.Authentication.signOut
import ca.judacribz.gainzassist.util.firebase.Database.setUserInfo
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

class Login : AppCompatActivity(), FacebookCallback<LoginResult>, FirebaseAuth.AuthStateListener {

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

    private var email: String? = null
    private var password: String? = null
    var linkGoogle = false

    // State for Compose
    private var uiState by mutableStateOf(LoginUiState())

    private val isFacebookEnabled: Boolean
        get() = BuildConfig.ENABLE_FACEBOOK_LOGIN.toBooleanStrictOrNull() ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)

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
        if (intent.getBooleanExtra(EXTRA_LOGOUT_USER, false)) {
            signInClient?.let { signOut(this, it) }
            if (isFacebookEnabled) {
                LoginManager.getInstance().logOut()
            }
        }
        auth?.addAuthStateListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            uiState = uiState.copy(isLoading = true)
            when (requestCode) {
                RC_SIGN_IN -> {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        val token = account?.idToken
                        if (token != null) {
                            googleCred = GoogleAuthProvider.getCredential(token, null)
                            val cred = googleCred
                            val client = signInClient
                            if (cred != null && client != null) {
                                signIn(this, cred, client)
                            } else {
                                authError("Google authentication failed: client or credential null")
                            }
                        } else {
                            authError("Google authentication failed: account or ID token null")
                        }
                    } catch (ex: ApiException) {
                        ex.printStackTrace()
                        uiState = uiState.copy(isLoading = false)
                        Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    callbackManager?.onActivityResult(requestCode, resultCode, data)
                }
            }
        } else {
            uiState = uiState.copy(isLoading = false)
        }
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
                credential?.let { linkUser(this, it, firebaseUser) }
            }
            setUserInfoPref(this, firebaseUser.email, firebaseUser.uid)
            setUserInfo(this)
            startActivity(Intent(this, Main::class.java))
            finish()
        }
    }

    override fun onSuccess(result: LoginResult) {
        credential = FacebookAuthProvider.getCredential(result.accessToken.token)
        val cred = credential
        val client = signInClient
        if (cred != null && client != null) {
            signIn(this, cred, client)
        } else {
            authError("Facebook authentication failed: client or credential null")
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
            startActivityForResult(signInIntent, RC_SIGN_IN)
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
            val client = signInClient
            if (cred != null && client != null) {
                signIn(this, cred, client)
            } else {
                authError("Email Login failed: client or credential null")
            }
        }
    }

    fun signUp() {
        val email = uiState.email.trim { it <= ' ' }
        val password = uiState.password.trim { it <= ' ' }
        if (validateForm(email, password)) {
            uiState = uiState.copy(isLoading = true)
            credential = EmailAuthProvider.getCredential(email, password)
            val client = signInClient
            if (client != null) {
                createUser(this, email, password, client)
            } else {
                authError("Sign Up failed: client uninitialized")
            }
        }
    }

    fun loginFail() {
        uiState = uiState.copy(isLoading = false)
    }
}
