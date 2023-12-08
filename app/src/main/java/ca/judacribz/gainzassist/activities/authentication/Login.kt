package ca.judacribz.gainzassist.activities.authentication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.OnClick
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.util.Preferences
import ca.judacribz.gainzassist.util.UI
import ca.judacribz.gainzassist.util.UI.ProgressHandler
import ca.judacribz.gainzassist.util.firebase.Authentication
import ca.judacribz.gainzassist.util.firebase.Database
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
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.GoogleAuthProvider
import java.io.IOException
import java.util.Objects

class Login : AppCompatActivity(), FacebookCallback<LoginResult>, AuthStateListener {
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

    // --------------------------------------------------------------------------------------------
    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UI.setInitView(this, R.layout.activity_login, null, false)
        progressHandler.setProgress(this, "Authenticating")

        // Setup main images
        setupSignInMethods()
        setupMainImages()
        ivLoginImg!!.post { loginSpring = UI.setSpring(ivLoginImg) }
        ivSignUpImg!!.post { signUpSpring = UI.setSpring(ivSignUpImg) }
        tvSignUpHere!!.post { loginScreen() }
    }

    private fun setupSignInMethods() {
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInOptions?.let { signInClient = GoogleSignIn.getClient(this, it) }

        // Configure Facebook Login
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, this)
        val accessToken = AccessToken.getCurrentAccessToken()
        isLoggedIn = accessToken != null && !accessToken.isExpired
    }

    private fun setupMainImages() {
        val assetManager = assets
        try {
            ivLoginImg!!.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(LOGIN_IMG)))
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        try {
            ivSignUpImg!!.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(SIGN_UP_IMG)))
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        slide_end = AnimationUtils.loadAnimation(this, R.anim.slide_end)
    }

    public override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(Main.EXTRA_LOGOUT_USER, false)) {
            signInClient?.let { Authentication.signOut(this, it) }
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
                        val account = task.getResult(
                            ApiException::class.java
                        )
                        googleCred = GoogleAuthProvider.getCredential(
                            Objects.requireNonNull(account)?.idToken,
                            null
                        )
                        signInClient?.let { Authentication.signIn(this, googleCred, it) }
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

    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////
    // FirebaseAuth.AuthStateListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Listener to handle all login types through firebase if successful */
    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val firebaseUser = firebaseAuth.currentUser

        // Current User is signed in
        if (firebaseUser != null) {
            progressHandler.show()
            Toast.makeText(
                this, String.format(getString(R.string.txt_logged_in), firebaseUser.email),
                Toast.LENGTH_SHORT
            ).show()
            if (linkGoogle) {
                Authentication.linkUser(this, credential, firebaseUser)
            }
            Preferences.setUserInfoPref(this, firebaseUser.email, firebaseUser.uid)
            Database.setUserInfo(this)
            startActivity(Intent(this, Main::class.java))
            finish()
            //            for (UserInfo profile : fbUser.getProviderData()) {
//                Toast.makeText(this, "Provider: " + profile.getProviderId(), Toast.LENGTH_SHORT).show();
//            }
        }
    }

    //FirebaseAuth.AuthStateListener//Override/////////////////////////////////////////////////////
    // FacebookCallback Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onSuccess(loginResult: LoginResult) {
        credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
        signInClient?.let { Authentication.signIn(this, credential, it) }
    }

    override fun onCancel() {}
    override fun onError(ex: FacebookException) {
        ex.printStackTrace()
    }

    //FacebookCallback//Override///////////////////////////////////////////////////////////////////
    /* Validates login and sign up forms using email and password combination */
    fun validateForm(email: String, password: String): Boolean {
        var emailIsValid = false
        var passwordIsValid = false
        if (email.isEmpty()) {
            etEmail!!.error = getString(R.string.err_required)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail!!.error = getString(R.string.err_required_email_format)
        } else {
            emailIsValid = true
        }
        if (password.isEmpty()) {
            etPassword!!.error = getString(R.string.err_required)
        } else if (password.length < MIN_PASSWORD_LEN) {
            etPassword!!.error = getString(R.string.err_required_password_min)
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
        btnFacebook!!.performClick()
    }

    @OnClick(R.id.btn_login)
    fun login() {
        email = etEmail!!.text.toString().trim { it <= ' ' }
        password = etPassword!!.text.toString().trim { it <= ' ' }
        if (validateForm(email!!, password!!)) {
            progressHandler.setTitle("Login")
            progressHandler.show()

            // Email/Password login to firebase
            credential = EmailAuthProvider.getCredential(email!!, password!!)
            signInClient?.let { Authentication.signIn(this, credential, it) }
            loginSpring!!.setEndValue(0.9)
        }
    }

    @OnClick(R.id.btn_sign_up)
    fun signUp() {
        email = etEmail!!.text.toString().trim { it <= ' ' }
        password = etPassword!!.text.toString().trim { it <= ' ' }
        if (validateForm(email!!, password!!)) {
            progressHandler.setTitle("Sign Up")
            progressHandler.show()
            credential = EmailAuthProvider.getCredential(email!!, password!!)
            // Email/Password sign up in firebase
            signInClient?.let { Authentication.createUser(this, email, password, it) }
            signUpSpring!!.setEndValue(0.9)
        }
    }

    @OnClick(R.id.tv_sign_up_here)
    fun signUpScreen() {
        animateView(btnSignUp, btnLogin, null)
        animateView(ivSignUpImg, ivLoginImg, null)
        animateView(tvSignUpQuest, tvLoginQuest, tvLoginHere)
        tvSignUpHere!!.visibility = View.INVISIBLE
    }

    @OnClick(R.id.iv_login_image, R.id.iv_sign_up_image)
    fun bounceImg() {
        loginSpring!!.setEndValue(0.3)
        signUpSpring!!.setEndValue(0.9)
    }

    @OnClick(R.id.tv_login_here)
    fun loginScreen() {
        animateView(btnLogin, btnSignUp, null)
        animateView(ivLoginImg, ivSignUpImg, null)
        animateView(tvLoginQuest, tvSignUpQuest, tvSignUpHere)
        tvLoginHere!!.visibility = View.INVISIBLE
    }

    /* Animates view elements as they become visible */
    fun animateView(inView: View?, outView: View?, navTextView: View?) {
        outView!!.visibility = View.INVISIBLE
        inView!!.visibility = View.VISIBLE
        val animator = ViewAnimationUtils.createCircularReveal(
            inView,
            inView.width / 2,
            inView.height / 2,
            0.0f, Math.hypot(inView.width.toDouble(), inView.height.toDouble()).toFloat()
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

    companion object {
        // Constants
        // --------------------------------------------------------------------------------------------
        private const val RC_SIGN_IN = 9001
        private const val MIN_PASSWORD_LEN = 6
        private const val SLIDE_DURATION = 1000
        private const val LOGIN_IMG = "squat.png"
        private const val SIGN_UP_IMG = "fatman.png"
        private const val PROGRESS_MAX = 100

        // --------------------------------------------------------------------------------------------
        // Global Vars
        // --------------------------------------------------------------------------------------------
        private val progressHandler = ProgressHandler()
    }
}
