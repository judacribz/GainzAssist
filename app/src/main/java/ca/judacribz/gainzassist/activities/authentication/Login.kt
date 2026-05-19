package ca.judacribz.gainzassist.activities.authentication

import android.animation.Animator
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.activities.main.Main.Companion.EXTRA_LOGOUT_USER
import ca.judacribz.gainzassist.databinding.ActivityLoginBinding
import ca.judacribz.gainzassist.util.Preferences.setUserInfoPref
import ca.judacribz.gainzassist.util.UI.ProgressHandler
import ca.judacribz.gainzassist.util.UI.setInitTheme
import ca.judacribz.gainzassist.util.UI.setSpring
import ca.judacribz.gainzassist.util.firebase.Authentication
import ca.judacribz.gainzassist.util.firebase.Authentication.RC_SIGN_IN
import ca.judacribz.gainzassist.util.firebase.Authentication.createUser
import ca.judacribz.gainzassist.util.firebase.Authentication.linkUser
import ca.judacribz.gainzassist.util.firebase.Authentication.signIn
import ca.judacribz.gainzassist.util.firebase.Authentication.signOut
import ca.judacribz.gainzassist.util.firebase.Database.setUserInfo
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.facebook.rebound.SpringUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import java.io.IOException
import java.util.*
import kotlin.math.hypot

class Login : AppCompatActivity(), FacebookCallback<LoginResult>, FirebaseAuth.AuthStateListener {

    companion object {
        private const val MIN_PASSWORD_LEN = 6
        private const val LOGIN_IMG = "squat.png"
        private const val SIGN_UP_IMG = "fatman.png"
    }

    private val progressHandler = ProgressHandler()

    private var auth: FirebaseAuth? = null
    private var credential: AuthCredential? = null
    private var googleCred: AuthCredential? = null
    private var signInClient: GoogleSignInClient? = null
    private var callbackManager: CallbackManager? = null

    private var email: String? = null
    private var password: String? = null
    private var slide_end: Animation? = null
    var linkGoogle = false
    private var loginSpring: Spring? = null
    private var signUpSpring: Spring? = null
    private var loginScreenRunnable: Runnable? = null

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressHandler.setProgress(this, "Authenticating", binding.blurLayout)

        setupSignInMethods()
        setupMainImages()

        binding.ivLoginImage.post { loginSpring = setSpring(binding.ivLoginImage) }
        binding.ivSignUpImage.post { signUpSpring = setSpring(binding.ivSignUpImage) }
        loginScreenRunnable = Runnable {
            if (!isFinishing && !isDestroyed && binding.root.isAttachedToWindow) {
                loginScreen()
            }
        }
        binding.tvSignUpHere.post(loginScreenRunnable!!)

        binding.btnGoogleSignIn.setOnClickListener { googleLogin() }
        binding.ibtnGoogle.setOnClickListener { googleLogin() }
        binding.ibtnFacebook.setOnClickListener { facebookLogin() }
        binding.btnLogin.setOnClickListener { login() }
        binding.btnSignUp.setOnClickListener { signUp() }
        binding.tvSignUpHere.setOnClickListener { signUpScreen() }
        binding.ivLoginImage.setOnClickListener { bounceImg() }
        binding.ivSignUpImage.setOnClickListener { bounceImg() }
        binding.tvLoginHere.setOnClickListener { loginScreen() }
    }

    private fun setupSignInMethods() {
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, this)
    }

    private fun setupMainImages() {
        val assetManager = assets
        try {
            binding.ivLoginImage.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(LOGIN_IMG)))
            binding.ivSignUpImage.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(SIGN_UP_IMG)))
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        slide_end = AnimationUtils.loadAnimation(this, R.anim.slide_end)
    }

    override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(EXTRA_LOGOUT_USER, false)) {
            signOut(this, signInClient!!)
            LoginManager.getInstance().logOut()
        }
        auth!!.addAuthStateListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                RC_SIGN_IN -> {
                    progressHandler.setTitle("Google Sign In")
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        googleCred = GoogleAuthProvider.getCredential(account!!.idToken, null)
                        signIn(this, googleCred!!, signInClient!!)
                    } catch (ex: ApiException) {
                        ex.printStackTrace()
                    }
                }
                else -> {
                    progressHandler.setTitle("Facebook Login")
                    callbackManager!!.onActivityResult(requestCode, resultCode, data)
                }
            }
            progressHandler.show()
        }
    }

    override fun onStop() {
        loginScreenRunnable?.let { binding.tvSignUpHere.removeCallbacks(it) }

        super.onStop()
        auth!!.removeAuthStateListener(this)
        progressHandler.dismiss()
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            progressHandler.show()
            Toast.makeText(
                this,
                String.format(getString(R.string.txt_logged_in), firebaseUser.email),
                Toast.LENGTH_SHORT
            ).show()
            if (linkGoogle) {
                linkUser(this, credential!!, firebaseUser)
            }
            setUserInfoPref(this, firebaseUser.email, firebaseUser.uid)
            setUserInfo(this)
            startActivity(Intent(this, Main::class.java))
            finish()
        }
    }

    override fun onSuccess(result: LoginResult) {
        credential = FacebookAuthProvider.getCredential(result.accessToken.token)
        signIn(this, credential!!, signInClient!!)
    }

    override fun onCancel() {}
    override fun onError(error: FacebookException) {
        error.printStackTrace()
    }

    fun validateForm(email: String, password: String): Boolean {
        var emailIsValid = false
        var passwordIsValid = false
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.err_required)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.err_required_email_format)
        } else {
            emailIsValid = true
        }
        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.err_required)
        } else if (password.length < MIN_PASSWORD_LEN) {
            binding.etPassword.error = getString(R.string.err_required_password_min)
        } else {
            passwordIsValid = true
        }
        return emailIsValid && passwordIsValid
    }

    fun googleLogin() {
        val signInIntent = signInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun facebookLogin() {
        binding.btnFacebookSignIn.performClick()
    }

    fun login() {
        email = binding.etEmail.text.toString().trim { it <= ' ' }
        password = binding.etPassword.text.toString().trim { it <= ' ' }
        if (validateForm(email!!, password!!)) {
            progressHandler.setTitle("Login")
            progressHandler.show()
            credential = EmailAuthProvider.getCredential(email!!, password!!)
            signIn(this, credential!!, signInClient!!)
            loginSpring?.endValue = 0.9
        }
    }

    fun signUp() {
        email = binding.etEmail.text.toString().trim { it <= ' ' }
        password = binding.etPassword.text.toString().trim { it <= ' ' }
        if (validateForm(email!!, password!!)) {
            progressHandler.setTitle("Sign Up")
            progressHandler.show()
            credential = EmailAuthProvider.getCredential(email!!, password!!)
            createUser(this, email!!, password!!, signInClient!!)
            signUpSpring?.endValue = 0.9
        }
    }

    fun signUpScreen() {
        animateView(binding.btnSignUp, binding.btnLogin, null)
        animateView(binding.ivSignUpImage, binding.ivLoginImage, null)
        // Original: tvSignUpQuest = tv_login_quest, tvLoginQuest = tv_sign_up_quest
        animateView(binding.tvLoginQuest, binding.tvSignUpQuest, binding.tvLoginHere)
        binding.tvSignUpHere.visibility = View.INVISIBLE
    }

    fun bounceImg() {
        loginSpring?.endValue = 0.3
        signUpSpring?.endValue = 0.9
    }

    fun loginScreen() {
        animateView(binding.btnLogin, binding.btnSignUp, null)
        animateView(binding.ivLoginImage, binding.ivSignUpImage, null)
        // Original: tvLoginQuest = tv_sign_up_quest, tvSignUpQuest = tv_login_quest
        animateView(binding.tvSignUpQuest, binding.tvLoginQuest, binding.tvSignUpHere)
        binding.tvLoginHere.visibility = View.INVISIBLE
    }

    fun animateView(inView: View, outView: View, navTextView: View?) {
        outView.visibility = View.INVISIBLE
        inView.visibility = View.VISIBLE

        if (!isFinishing &&
            !isDestroyed &&
            inView.isAttachedToWindow &&
            inView.width > 0 &&
            inView.height > 0
        ) {
            val animator = ViewAnimationUtils.createCircularReveal(
                inView,
                inView.width / 2,
                inView.height / 2,
                0.0f,
                hypot(
                    (inView.width / 2).toDouble(),
                    (inView.height / 2).toDouble()
                ).toFloat() * 2
            )
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()
        }

        if (navTextView != null) {
            slide_end!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    navTextView.setPadding(100, 0, 0, 0)
                    navTextView.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
            })

            if (navTextView.isAttachedToWindow) {
                navTextView.startAnimation(slide_end)
            } else {
                navTextView.setPadding(100, 0, 0, 0)
                navTextView.visibility = View.VISIBLE
            }
        }
    }

    fun loginFail() {
        progressHandler.dismiss()
    }
}
