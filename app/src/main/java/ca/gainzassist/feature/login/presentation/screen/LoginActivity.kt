package ca.gainzassist.feature.login.presentation.screen

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import ca.gainzassist.BuildConfig
import ca.gainzassist.R
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.data.local.preferences.Preferences
import ca.gainzassist.data.remote.firebase.Authentication
import ca.gainzassist.data.remote.firebase.Database
import ca.gainzassist.feature.login.presentation.event.LoginActions
import ca.gainzassist.feature.login.presentation.viewmodel.LoginViewModel
import ca.gainzassist.feature.login.presentation.viewmodel.LoginViewModelEvent
import ca.gainzassist.feature.main.presentation.screen.MainActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

class LoginActivity : GainzBaseActivity(), FacebookCallback<LoginResult>,
    FirebaseAuth.AuthStateListener {

    private val loginViewModel: LoginViewModel by viewModel()

    private var auth: FirebaseAuth? = null
    private var credential: AuthCredential? = null
    private var googleCred: AuthCredential? = null
    private var callbackManager: CallbackManager? = null
    private var loginBitmap: Bitmap? = null
    private var signUpBitmap: Bitmap? = null

    var linkGoogle = false

    private val isFacebookEnabled: Boolean
        get() = BuildConfig.ENABLE_FACEBOOK_LOGIN.toBooleanStrictOrNull() ?: false

    override fun configureEdgeToEdge() = enableEdgeToEdge(
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )

    override fun onBeforeSetContent() {
        loginViewModel.setFacebookEnabled(isFacebookEnabled)
        setupSignInMethods()
        loginBitmap = loadBitmapFromAssets(LOGIN_IMG)
        signUpBitmap = loadBitmapFromAssets(SIGN_UP_IMG)
    }

    @Composable
    override fun AppContent() = InnerContent()

    @Composable
    override fun InnerContent() {
        val state by loginViewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            loginViewModel.events.collect { event ->
                when (event) {
                    is LoginViewModelEvent.NavigateToMain -> {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }

                    is LoginViewModelEvent.ShowToast, is LoginViewModelEvent.AuthError -> {
                        val message = when (event) {
                            is LoginViewModelEvent.ShowToast -> event.message
                            is LoginViewModelEvent.AuthError -> event.message
                        }
                        message.let {
                            Toast.makeText(
                                /* context = */ this@LoginActivity,
                                /* text = */ it.asString(this@LoginActivity),
                                /* duration = */ Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is LoginViewModelEvent.StartGoogleLogin -> {
                        googleLogin()
                    }

                    is LoginViewModelEvent.StartFacebookLogin -> {
                        facebookLogin()
                    }
                }
            }
        }

        LoginScreen(
            state = state,
            loginImage = loginBitmap,
            signUpImage = signUpBitmap,
            actions = object : LoginActions {
                override fun onEmailChanged(email: String) = loginViewModel.onEmailChanged(email)
                override fun onPasswordChanged(password: String) = loginViewModel.onPasswordChanged(password)
                override fun onToggleMode() = loginViewModel.onToggleMode()
                override fun onLoginClick() = loginViewModel.onLoginClick()
                override fun onSignUpClick() = loginViewModel.onSignUpClick()
                override fun onGoogleSignInClick() = loginViewModel.onGoogleSignInClick()
                override fun onFacebookSignInClick() = loginViewModel.onFacebookSignInClick()
                override fun onImageBounceClick() = loginViewModel.onImageBounceClick()
            }
        )
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
            loginViewModel.setLoading(true)
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
        loginViewModel.setLoading(false)
    }

    override fun onError(error: FacebookException) {
        error.printStackTrace()
        loginViewModel.setLoading(false)
        Toast.makeText(
            /* context = */ this,
            /* text = */ getString(R.string.err_facebook_login_failed),
            /* duration = */ Toast.LENGTH_SHORT
        ).show()
    }

    fun googleLogin() {
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
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(
                        data = credential.data
                    )
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
                loginViewModel.setLoading(false)
                Toast.makeText(
                    /* context = */ this@LoginActivity,
                    /* text = */getString(R.string.err_google_login_failed),
                    /* duration = */Toast.LENGTH_SHORT
                ).show()
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

    fun loginFail() {
        loginViewModel.setLoading(false)
    }

    private fun loadBitmapFromAssets(fileName: String): Bitmap? = try {
        BitmapFactory.decodeStream(assets.open(fileName))
    } catch (ioe: IOException) {
        ioe.printStackTrace()
        null
    }

    private fun setupSignInMethods() {
        auth = FirebaseAuth.getInstance()
        if (isFacebookEnabled) {
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager, this)
        }
    }

    private fun authError(message: String) {
        Logger.e(message)
        loginViewModel.setLoading(false)
        Toast.makeText(this, getString(R.string.err_auth_failed), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val LOGIN_IMG = "squat.png"
        private const val SIGN_UP_IMG = "fatman.png"
    }
}
