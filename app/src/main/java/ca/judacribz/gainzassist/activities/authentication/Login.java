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
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.*;
import ca.judacribz.gainzassist.models.*;
import static ca.judacribz.gainzassist.Main.EXTRA_LOGOUT_USER;
import static ca.judacribz.gainzassist.firebase.Authentication.*;
import static ca.judacribz.gainzassist.firebase.Database.*;
import static ca.judacribz.gainzassist.util.UI.setToolbar;

public class Login extends AppCompatActivity implements FacebookCallback<LoginResult>,
                                                        FirebaseAuth.AuthStateListener,
                                                        View.OnClickListener {
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

    WorkoutHelper workoutHelper;
    boolean linkGoogle;

    @BindView(R.id.tv_sign_up_here) TextView tvSignUpHere;
    @BindView(R.id.tv_login_here) TextView tvLoginHere;
    @BindView(R.id.tv_sign_up_quest) TextView tvLoginQuest;
    @BindView(R.id.tv_login_quest) TextView tvSignUpQuest;

    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_password) EditText etPassword;

    @BindView(R.id.iv_login_image) ImageView ivLoginImg;
    @BindView(R.id.iv_sign_up_image) ImageView ivSignUpImg;

    @BindView(R.id.btn_google_sign_in) SignInButton btnGoogle;
    @BindView(R.id.btn_facebook_login) LoginButton btnFacebook;
    @BindView(R.id.btn_login) Button btnLogin;
    @BindView(R.id.btn_sign_up) Button btnSignUp;
    // --------------------------------------------------------------------------------------------


    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setToolbar(this, R.string.app_name, false);

        tvSignUpHere.setOnClickListener(this);
        tvLoginHere.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        btnFacebook.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);

        // Get firebase instance and setup google and facebook sign in
        setupSignInMethods();

        // Setup main images
        setupMainImages();
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
        callBackManager = CallbackManager.Factory.create();
        btnFacebook.setReadPermissions("email");
        btnFacebook.registerCallback(callBackManager, this);
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


                        signIn(this, googleCred);

                    } catch (ApiException ex) {
                        ex.printStackTrace();
                    }
                    break;

                default:
                    callBackManager.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(this);
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////


    // FacebookCallback Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSuccess(LoginResult loginResult) {
        credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
        signIn(this, credential);
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
        FirebaseUser fbUser = firebaseAuth.getCurrentUser();

        // User is signed in
        if (fbUser != null) {

            if (linkGoogle) {
                fbUser.linkWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                            }
                        });
            }
            for (UserInfo profile : fbUser.getProviderData()) {
                Toast.makeText(this, "Provider: " + profile.getProviderId(), Toast.LENGTH_SHORT).show();
            }


            final String email = fbUser.getEmail();
            final String uid = fbUser.getUid();
            Toast.makeText(this,
                    String.format(getString(R.string.txt_logged_in), email),
                    Toast.LENGTH_SHORT).show();

            // Set email for singleton User
            User user = User.getInstance();
            user.setEmail(email);
            user.setUid(uid);

            // Get local db instance
            workoutHelper = new WorkoutHelper(getApplicationContext());

            /* If the db doesn't exist, or the users email does not exist in the db then get
             * reference to the default workouts from firebase and create the local db using
             * these values */
            if (!workoutHelper.exists() || !workoutHelper.emailExists()) {

                DatabaseReference userRef =
                        FirebaseDatabase.getInstance().getReference(String.format(USER_PATH, uid));

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // FIREBASE: if user does not exist copy default workouts to under user and
                        // into local db
                        if (dataSnapshot.getValue() == null) {
                            setFirebaseWorkouts(Login.this, DEFAULT_WORKOUTS);

                            // FIREBASE: if user exists, use users workouts to save in database
                        } else {
                            setFirebaseWorkouts(Login.this, String.format(USER_WORKOUTS_PATH, uid));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            startActivity(new Intent(this, Main.class));
            finish();
        }
    }
    //FirebaseAuth.AuthStateListener//Override/////////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================
    /* Wraps handleClick */
    @Override
    public void onClick(View v) {
        handleClick(v.getId());
    }

    /* Handles all clicks in activity */
    public void handleClick(int id) {
        switch (id) {
            case R.id.btn_google_sign_in:
                googleSignIn(false);
                break;

            case R.id.btn_login:
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                if (validateForm(email, password)) {

                    // Email/Password login to firebase
                    credential = EmailAuthProvider.getCredential(email, password);

                    signIn(this, credential);
                }
                break;

            case R.id.btn_sign_up:
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                if (validateForm(email, password)) {

                    // Email/Password sign up in firebase
                    createUser(this, email, password);
                }
                break;

            case R.id.tv_sign_up_here:
                changeView(true);
                break;

            case R.id.tv_login_here:
                changeView(false);
                break;
        }
    }

    public void googleSignIn(boolean linkAccount) {
        this.linkGoogle = linkAccount;
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

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

    /* Changes the view from the login view to sign up or vice versa */
    public void changeView(boolean hideLogin) {
        if (hideLogin) {
            animateView(btnSignUp, btnLogin, null);
            animateView(ivSignUpImg, ivLoginImg, null);
            animateView(tvLoginQuest, tvSignUpQuest, tvLoginHere);
            tvSignUpHere.setVisibility(View.INVISIBLE);

        } else {
            animateView(btnLogin, btnSignUp, null);
            animateView(ivLoginImg, ivSignUpImg, null);
            animateView(tvSignUpQuest, tvLoginQuest, tvSignUpHere);
            tvLoginHere.setVisibility(View.INVISIBLE);
        }
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
        animator.setDuration(SLIDE_DURATION);
        animator.start();


        if (navTextView != null) {
            slide_end.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    navTextView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            navTextView.startAnimation(slide_end);
        }
    }
    //=Click=Handling==============================================================================
}
