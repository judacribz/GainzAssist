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
import android.widget.Toast;;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.rebound.Spring;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import java.io.IOException;
import java.util.Objects;
import butterknife.BindView;
import butterknife.OnClick;

import ca.judacribz.gainzassist.*;
import ca.judacribz.gainzassist.R;

import static ca.judacribz.gainzassist.Main.EXTRA_LOGOUT_USER;
import static ca.judacribz.gainzassist.util.UI.setSpring;
import static ca.judacribz.gainzassist.util.firebase.Authentication.*;
import static ca.judacribz.gainzassist.util.firebase.Database.setUserInfo;
import static ca.judacribz.gainzassist.util.Preferences.*;
import static ca.judacribz.gainzassist.util.UI.setInitView;

public class Login extends AppCompatActivity implements FacebookCallback<LoginResult>,
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
    AuthCredential credential, googleCred;
    GoogleSignInOptions signInOptions;
    GoogleSignInClient signInClient;
    CallbackManager callbackManager;

    String email, password;
    Animation slide_end;
    boolean isLoggedIn;
    public boolean linkGoogle;
    Spring loginSpring, signUpSpring;

    @BindView(R.id.tv_sign_up_here) TextView tvSignUpHere;
    @BindView(R.id.tv_login_here) TextView tvLoginHere;
    @BindView(R.id.tv_sign_up_quest) TextView tvLoginQuest;
    @BindView(R.id.tv_login_quest) TextView tvSignUpQuest;

    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_password) EditText etPassword;

    @BindView(R.id.iv_login_image) ImageView ivLoginImg;
    @BindView(R.id.iv_sign_up_image) ImageView ivSignUpImg;

    @BindView(R.id.btn_google_sign_in) SignInButton btnGoogle;
    @BindView(R.id.btn_facebook_sign_in) LoginButton btnFacebook;
    @BindView(R.id.btn_login) Button btnLogin;
    @BindView(R.id.btn_sign_up) Button btnSignUp;

    @BindView(R.id.progress_bar) ProgressBar progressBar;
    // --------------------------------------------------------------------------------------------


    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, R.layout.activity_login, R.string.app_name,  false);

        progressBar.setMax(10);

        // Get firebase instance and setup google and facebook sign in
        new Thread(new Runnable() {
            @Override
            public void run() {
                setupSignInMethods();
            }
        }).start();

        // Setup main images
        new Thread(new Runnable() {
            @Override
            public void run() {
                setupMainImages();
            }
        }).start();

        ivLoginImg.post(new Runnable() {
            @Override
            public void run() {
                loginSpring = setSpring(ivLoginImg);
            }
        });

        ivSignUpImg.post(new Runnable() {
            @Override
            public void run() {
                signUpSpring = setSpring(ivSignUpImg);
            }
        });

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

        // Configure Facebook Login
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        isLoggedIn = accessToken != null && !accessToken.isExpired();
    }

    private void setupMainImages() {
        AssetManager assetManager = getAssets();
        try {
            ivLoginImg.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(LOGIN_IMG)));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        try {
            ivSignUpImg.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(SIGN_UP_IMG)));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        slide_end = AnimationUtils.loadAnimation(this, R.anim.slide_end);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getIntent().getBooleanExtra(EXTRA_LOGOUT_USER, false)) {
            signOut(this, signInClient);
            LoginManager.getInstance().logOut();
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
                        googleCred = GoogleAuthProvider.getCredential(Objects.requireNonNull(account).getIdToken(), null);


                        signIn(this, googleCred, signInClient);

                    } catch (ApiException ex) {
                        ex.printStackTrace();
                    }
                    break;
                default:
                    callbackManager.onActivityResult(requestCode, resultCode, data);
                    break;
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
    @Override
    public void onSuccess(LoginResult loginResult) {
        credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
        signIn(this, credential, signInClient);
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onError(FacebookException ex) {
        ex.printStackTrace();
    }
    //FacebookCallback//Override///////////////////////////////////////////////////////////////////


    // FirebaseAuth.AuthStateListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Listener to handle all login types through firebase if successful */
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Current User is signed in
        if (firebaseUser != null) {
            Toast.makeText(
                    this,
                    String.format(getString(R.string.txt_logged_in), firebaseUser.getEmail()),
                    Toast.LENGTH_SHORT
            ).show();

            if (linkGoogle) {
                linkUser(this, credential, firebaseUser);
            }

            setUserInfoPref(this, firebaseUser.getEmail(), firebaseUser.getUid());

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
    public void googleLogin() {
       googleSignIn(this, signInClient);
    }

    @OnClick(R.id.btn_facebook_sign_in)
    public void facebookLogin() {
//        facebookSignIn(this);
    }

    @OnClick(R.id.btn_login)
    public void login() {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if (validateForm(email, password)) {
            // Email/Password login to firebase
            credential = EmailAuthProvider.getCredential(email, password);
            signIn(this, credential, signInClient);
            loginSpring.setEndValue(0.9);
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

            signUpSpring.setEndValue(0.9);
        }
    }

    @OnClick(R.id.tv_sign_up_here)
    public void signUpScreen() {
        animateView(btnSignUp, btnLogin, null);
        animateView(ivSignUpImg, ivLoginImg, null);
        animateView(tvSignUpQuest, tvLoginQuest, tvLoginHere);
        tvSignUpHere.setVisibility(View.INVISIBLE);
    }

    @OnClick({R.id.iv_login_image, R.id.iv_sign_up_image})
    public void bounceImg() {
        loginSpring.setEndValue(0.3);
        signUpSpring.setEndValue(0.9);
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
