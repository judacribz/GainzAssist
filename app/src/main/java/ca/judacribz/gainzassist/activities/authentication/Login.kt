package ca.judacribz.gainzassist.activities.authentication

import android.animation.Animator
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.activities.main.Main.EXTRA_LOGOUT_USER
import ca.judacribz.gainzassist.util.Preferences.setUserInfoPref
import ca.judacribz.gainzassist.util.UI.ProgressHandler
import ca.judacribz.gainzassist.util.UI.setInitView
import ca.judacribz.gainzassist.util.UI.setSpring
import ca.judacribz.gainzassist.util.firebase.Authentication.*
import ca.judacribz.gainzassist.util.firebase.Database.setUserInfo
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.facebook.rebound.Spring
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import java.io.IOException
import butterknife.BindView
import butterknife.OnClick

class Login : AppCompatActivity(), FacebookCallback<LoginResult?>, FirebaseAuth.AuthStateListener {

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val MIN_PASSWORD_LEN = 6
        private const val SLIDE_DURATION = 1000

        private const val LOGIN_IMG = "squat.png"
        private const val SIGN_UP_IMG = "fatman.png"

        private const val PROGRESS_MAX = 100
        
        private val progressHandler = ProgressHandler()
    }

    var auth: FirebaseAuth? = null
    var credential: AuthCredential? = null
    var googleCred: AuthCredential? = null
    var signInOptions: GoogleSignInOptions? = null
    var signInClient: GoogleSignInClient? = null
    var callbackManager: CallbackManager? = null

    var email: String? = null
    var password: String? = null
    var slide_end: Animation? = null
    var isLoggedIn = false
    @JvmField
    var linkGoogle = false
    var loginSpring: Spring? = null
    var signUpSpring: Spring? = null

    @BindView(R.id.blurLayout)
    lateinit var blurLayout: View

    @BindView(R.id.tv_sign_up_here)
    lateinit var tvSignUpHere: TextView

    @BindView(R.id.tv_login_here)
    lateinit var tvLoginHere: TextView

    @BindView(R.id.tv_sign_up_quest)
    lateinit var tvLoginQuest: TextView

    @BindView(R.id.tv_login_quest)
    lateinit var tvSignUpQuest: TextView

    @BindView(R.id.et_email)
    lateinit var etEmail: EditText

    @BindView(R.id.et_password)
    lateinit var etPassword: EditText

    @BindView(R.id.iv_login_image)
    lateinit var ivLoginImg: ImageView

    @BindView(R.id.iv_sign_up_image)
    lateinit var ivSignUpImg: ImageView

    @BindView(R.id.btn_google_sign_in)
    lateinit var btnGoogle: SignInButton

    @BindView(R.id.btn_facebook_sign_in)
    lateinit var btnFacebook: LoginButton

    @BindView(R.id.ibtn_facebook)
    lateinit var ibtnFacebook: ImageButton

    @BindView(R.id.ibtn_google)
    lateinit var ibtnGoogle: ImageButton

    @BindView(R.id.btn_login)
    lateinit var btnLogin: Button

    @BindView(R.id.btn_sign_up)
    lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(this, R.layout.activity_login, null, false)
        progressHandler.setProgress(this, "Authenticating", blurLayout)

        setupSignInMethods()
        setupMainImages()

        ivLoginImg.post { loginSpring = setSpring(ivLoginImg) }
        ivSignUpImg.post { signUpSpring = setSpring(ivSignUpImg) }
        tvSignUpHere.post { loginScreen() }
    }

    private fun setupSignInMethods() {
        auth = FirebaseAuth.getInstance()

        signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, signInOptions!!)

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, this)
        val accessToken = AccessToken.getCurrentAccessToken()
        isLoggedIn = accessToken != null && !accessToken.isExpired
    }

    private fun setupMainImages() {
        val assetManager = assets
        try {
            ivLoginImg.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(LOGIN_IMG)))
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

        try {
            ivSignUpImg.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(SIGN_UP_IMG)))
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

        slide_end = AnimationUtils.loadAnimation(this, R.anim.slide_end)
    }

    override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(EXTRA_LOGOUT_USER, false)) {
            signOut(this, signInClient)
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
                        signIn(this, googleCred, signInClient)
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
                linkUser(this, credential, firebaseUser)
            }

            setUserInfoPref(this, firebaseUser.email, firebaseUser.uid)
            setUserInfo(this)

            startActivity(Intent(this, Main::class.java))
            finish()
        }
    }

    override fun onSuccess(result: LoginResult?) {
        credential = FacebookAuthProvider.getCredential(result!!.accessToken.token)
        signIn(this, credential, signInClient)
    }

    override fun onCancel() {}

    override fun onError(error: FacebookException) {
        error.printStackTrace()
    }

    fun validateForm(email: String, password: String): Boolean {
        var emailIsValid = false
        var passwordIsValid = false

        if (email.isEmpty()) {
            etEmail.error = getString(R.string.err_required)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.err_required_email_format)
        } else {
            emailIsValid = true
        }

        if (password.isEmpty()) {
            etPassword.error = getString(R.string.err_required)
        } else if (password.length < MIN_PASSWORD_LEN) {
            etPassword.error = getString(R.string.err_required_password_min)
        } else {
            passwordIsValid = true
        }

        return emailIsValid && passwordIsValid
    }

    @OnClick(R.id.btn_google_sign_in, R.id.ibtn_google)
    fun googleLogin() {
        val signInIntent = signInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @OnClick(R.id.ibtn_facebook)
    fun facebookLogin() {
        btnFacebook.performClick()
    }

    @OnClick(R.id.btn_login)
    fun login() {
        email = etEmail.text.toString().trim { it <= ' ' }
        password = etPassword.text.toString().trim { it <= ' ' }
        if (validateForm(email!!, password!!)) {
            progressHandler.setTitle("Login")
            progressHandler.show()

            credential = EmailAuthProvider.getCredential(email!!, password!!)
            signIn(this, credential, signInClient)
            loginSpring!!.endValue = 0.9
        }
    }

    @OnClick(R.id.btn_sign_up)
    fun signUp() {
        email = etEmail.text.toString().trim { it <= ' ' }
        password = etPassword.text.toString().trim { it <= ' ' }
        if (validateForm(email!!, password!!)) {
            progressHandler.setTitle("Sign Up")
            progressHandler.show()

            credential = EmailAuthProvider.getCredential(email!!, password!!)
            createUser(this, email, password, signInClient)
            signUpSpring!!.endValue = 0.9
        }
    }

    @OnClick(R.id.tv_sign_up_here)
    fun signUpScreen() {
        animateView(btnSignUp, btnLogin, null)
        animateView(ivSignUpImg, ivLoginImg, null)
        animateView(tvSignUpQuest, tvLoginQuest, tvLoginHere)
        tvSignUpHere.visibility = View.INVISIBLE
    }

    @OnClick(R.id.iv_login_image, R.id.iv_sign_up_image)
    fun bounceImg() {
        loginSpring!!.endValue = 0.3
        signUpSpring!!.endValue = 0.9
    }

    @OnClick(R.id.tv_login_here)
    fun loginScreen() {
        animateView(btnLogin, btnSignUp, null)
        animateView(ivLoginImg, ivSignUpImg, null)
        animateView(tvLoginQuest, tvSignUpQuest, tvSignUpHere)
        tvLoginHere.visibility = View.INVISIBLE
    }

    fun animateView(inView: View, outView: View, navTextView: View?) {
        outView.visibility = View.INVISIBLE
        inView.visibility = View.VISIBLE
        val animator = ViewAnimationUtils.createCircularReveal(
            inView,
            inView.width / 2,
            inView.height / 2,
            0.0f,
            Math.hypot(inView.width.toDouble(), inView.height.toDouble()).toFloat()
        )
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()

        if (navTextView != null) {
            slide_end!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    navTextView.setPadding(100, 0, 0, 0)
                    navTextView.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
            })
            navTextView.startAnimation(slide_end)
        }
    }

    fun loginFail() {
        progressHandler.dismiss()
    }
}
