package ca.judacribz.gainzassist.activities.authentication;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import ca.judacribz.gainzassist.async.FirebaseService;
import ca.judacribz.gainzassist.models.WorkoutHelper;
import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.OnClick;

import ca.judacribz.gainzassist.*;

import static ca.judacribz.gainzassist.firebase.Authentication.*;

import static ca.judacribz.gainzassist.Main.EXTRA_LOGOUT_USER;
import static ca.judacribz.gainzassist.firebase.Database.setUserInfo;
import static ca.judacribz.gainzassist.util.UI.setInitView;

public class Login extends AppCompatActivity implements /*FacebookCallback<LoginResult>,*/
                                                        FirebaseAuth.AuthStateListener {
    // Constants
    // --------------------------------------------------------------------------------------------
    private static final int RC_SIGN_IN = 9001;
    private static final int MIN_PASSWORD_LEN = 6;
    private static final int SLIDE_DURATION = 1000;
    private static final String LOGIN_IMG = "squat.png";
    private static final String SIGN_UP_IMG = "fatman.png";
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    FirebaseAuth auth;
    AuthCredential credential;
    AuthCredential googleCred;
    GoogleSignInOptions signInOptions;
    GoogleSignInClient signInClient;
    CallbackManager callBackManager;

    String email, password;
    Animation slide_end;

    public boolean linkGoogle;

    @BindView(R.id.tv_sign_up_here) TextView tvSignUpHere;
    @BindView(R.id.tv_login_here) TextView tvLoginHere;
    @BindView(R.id.tv_sign_up_quest) TextView tvLoginQuest;
    @BindView(R.id.tv_login_quest) TextView tvSignUpQuest;

    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_password) EditText etPassword;

    @BindView(R.id.iv_login_image) ImageView ivLoginImg;
    @BindView(R.id.iv_sign_up_image) ImageView ivSignUpImg;

    @BindView(R.id.btn_google_sign_in) SignInButton btnGoogle;
//    @BindView(R.id.btn_facebook_login) LoginButton btnFacebook;
    @BindView(R.id.btn_login) Button btnLogin;
    @BindView(R.id.btn_sign_up) Button btnSignUp;


    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    // --------------------------------------------------------------------------------------------


    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, R.layout.activity_login, R.string.app_name,  false);

        progressBar.setMax(10);
        // Get firebase instance and setup google and facebook sign in
        setupSignInMethods();

        // Setup main images
        setupMainImages();

        tvSignUpHere.post(new Runnable() {
            @Override
            public void run() {
                loginScreen();
            }
        });
    }

    private void setupSignInMethods() {
        auth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);

//        // Configure Facebook Login
//        callBackManager = CallbackManager.Factory.create();
//        btnFacebook.setReadPermissions("email");
//        btnFacebook.registerCallback(callBackManager, this);
    }

    private void setupMainImages() {
        AssetManager assetManager = getAssets();
        try {
            ivLoginImg.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(LOGIN_IMG)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            ivSignUpImg.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(SIGN_UP_IMG)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        slide_end = AnimationUtils.loadAnimation(this, R.anim.slide_end);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getIntent().getBooleanExtra(EXTRA_LOGOUT_USER, false)) {
            signOut(this, signInClient);
        }
        auth.addAuthStateListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_SIGN_IN:
                    Task<GoogleSignInAccount> task
                            = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        googleCred = GoogleAuthProvider.getCredential(account.getIdToken(), null);


                        signIn(this, googleCred, signInClient);

                    } catch (ApiException ex) {
                        ex.printStackTrace();
                    }
                    break;
//                default:
//                    callBackManager.onActivityResult(requestCode, resultCode, data);
//                    break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(this);
        progressBar.setVisibility(View.GONE);
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////


    // FacebookCallback Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
//    @Override
//    public void onSuccess(LoginResult loginResult) {
//        credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
//        signIn(this, credential);
//    }
//
//    @Override
//    public void onCancel() {
//    }
//
//    @Override
//    public void onError(FacebookException ex) {
//        ex.printStackTrace();
//    }
    //FacebookCallback//Override///////////////////////////////////////////////////////////////////


    // FirebaseAuth.AuthStateListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Listener to handle all login types through firebase if successful */
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser fbUser = firebaseAuth.getCurrentUser();

        // CurrUser is signed in
        if (fbUser != null) {
            Toast.makeText(
                    this,
                    String.format(getString(R.string.txt_logged_in), fbUser.getEmail()),
                    Toast.LENGTH_SHORT
            ).show();

            // Run in bg, start Main activity once database is loaded

            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            setUserInfo(this);

            startActivity(new Intent(this, Main.class));
            finish();

//            for (UserInfo profile : fbUser.getProviderData()) {
//                Toast.makeText(this, "Provider: " + profile.getProviderId(), Toast.LENGTH_SHORT).show();
//            }
        }
    }
    //FirebaseAuth.AuthStateListener//Override/////////////////////////////////////////////////////

    /* Validates login and sign up forms using email and password combination */
    public boolean validateForm(String email, String password) {
        boolean emailIsValid = false;
        boolean passwordIsValid = false;

        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.err_required));

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_required_email_format));

        } else {
            emailIsValid = true;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.err_required));

        } else if (password.length() < MIN_PASSWORD_LEN) {
            etPassword.setError(getString(R.string.err_required_password_min));

        } else {
            passwordIsValid = true;
        }

        return emailIsValid && passwordIsValid;
    }


    @OnClick(R.id.btn_google_sign_in)
    public void googSignIn() {
        googleSignIn(this, signInClient);
    }

    @OnClick(R.id.btn_login)
    public void login() {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if (validateForm(email, password)) {
            // Email/Password login to firebase
            credential = EmailAuthProvider.getCredential(email, password);
            signIn(this, credential, signInClient);
        }
    }
    @OnClick(R.id.btn_sign_up)
    public void signUp() {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if (validateForm(email, password)) {
            credential = EmailAuthProvider.getCredential(email, password);
            // Email/Password sign up in firebase
            createUser(this, email, password, signInClient);
        }
    }

    @OnClick(R.id.tv_sign_up_here)
    public void signUpScreen() {
        animateView(btnSignUp, btnLogin, null);
        animateView(ivSignUpImg, ivLoginImg, null);
        animateView(tvSignUpQuest, tvLoginQuest, tvLoginHere);
        tvSignUpHere.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.tv_login_here)
    public void loginScreen() {
        animateView(btnLogin, btnSignUp, null);
        animateView(ivLoginImg, ivSignUpImg, null);
        animateView(tvLoginQuest, tvSignUpQuest, tvSignUpHere);
        tvLoginHere.setVisibility(View.INVISIBLE);
    }

    /* Animates view elements as they become visible */
    public void animateView(final View inView, final View outView, @Nullable final View navTextView) {
        outView.setVisibility(View.INVISIBLE);
        inView.setVisibility(View.VISIBLE);
        Animator animator = ViewAnimationUtils.createCircularReveal(
                inView,
                inView.getWidth()/2,
                inView.getHeight()/2,
                0.0f,
                (float) Math.hypot(inView.getWidth(), inView.getHeight())
        );
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();


        if (navTextView != null) {
            slide_end.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    navTextView.setPadding(100,0, 0, 0);
                    navTextView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            navTextView.startAnimation(slide_end);
        }
    }

}
