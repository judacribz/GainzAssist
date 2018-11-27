package ca.judacribz.gainzassist.util.firebase;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import ca.judacribz.gainzassist.background.FirebaseService;
import ca.judacribz.gainzassist.models.WorkoutHelper;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.*;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.authentication.Login;

import java.util.Arrays;


public class Authentication {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final int MIN_PASSWORD_LEN = 6;
    public static final int RC_SIGN_IN = 9001;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private static boolean userCreated = false;
    // --------------------------------------------------------------------------------------------


    /* Wrapper function to create an user in Firebase */
    public static void createUser(final Activity act, String email, String password, final GoogleSignInClient signInClient) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(act, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        handleOnComplete(act, signInClient, task);
                    }
                });
    }

    /* Wrapper function to log in an user using a firebase credential */
    public static void signIn(final Activity act, AuthCredential cred, final GoogleSignInClient signInClient) {
        mAuth.signInWithCredential(cred)
                .addOnCompleteListener(act, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        handleOnComplete(act, signInClient, task);
                    }
                });
    }

    private static void handleOnComplete(
            Activity act,
            GoogleSignInClient signInClient,
            @NonNull Task<AuthResult> task) {
        String msg;

        // Sign up success
        if (task.isSuccessful()) {
            userCreated = true;
            msg = act.getString(R.string.sign_up_success);

            // Sign up fail
        } else {
            userCreated = false;
            msg = getExceptionMsg(act, task.getException());
        }

        Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(act.getString(R.string.txt_email_registered))) {
            ((Login) act).linkGoogle = true;
            googleSignIn(act, signInClient);
        }
    }

    public static void linkUser(final Activity act, AuthCredential cred, FirebaseUser currUser) {
        currUser.linkWithCredential(cred)
                .addOnCompleteListener(act, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(act, "Linked!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(act, "linkWithCredential:failure" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public static void googleSignIn(Activity act, GoogleSignInClient signInClient) {
        Intent signInIntent = signInClient.getSignInIntent();
        act.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

//    public static void facebookSignIn(Activity act) {
//        LoginManager.getInstance().logInWithReadPermissions(
//                act,
//                Arrays.asList("email", "public_profile")
//        );
//    }

    /* Wrapper function to sign out user */
    public static void signOut(Activity act, GoogleSignInClient signInClient) {
        FirebaseAuth.getInstance().signOut();

        signInClient.signOut().addOnCompleteListener(
                act,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                }
        );

        act.stopService(new Intent(act, FirebaseService.class));
        (new WorkoutHelper(act)).deleteAllWorkouts();
    }

    /* Used by sign in and sign up to firebase to handle exception if they fail */
    private static String getExceptionMsg(Activity act, Exception taskEx) {
        String msg = "";

        if (taskEx != null) {
            try {
                throw taskEx;

            // No internet connection
            } catch (FirebaseNetworkException ex) {
                msg = act.getString(R.string.txt_network_needed);

            // Password does not meet minimum requirements
            } catch (FirebaseAuthWeakPasswordException ex) {
                msg = act.getString(R.string.err_invalid_password);

            // Email is already registered
            } catch (FirebaseAuthUserCollisionException ex) {
                msg = act.getString(R.string.txt_email_registered);

            // Email format is invalid
            } catch (FirebaseAuthInvalidCredentialsException ex) {
                msg = act.getString(R.string.err_invalid_email);

            // Other exceptions
            } catch (Exception ex) {
                msg = ex.toString();
            }
        }

        return msg;
    }

//    /* Validates login and sign up forms using email and password combination */
//    public static boolean validateForm(Activity activity, EditText txtEmail, EditText txtPassword) {
//        boolean formIsValid = false;
//        String email = txtEmail.getText().toString().trim();
//        String password = txtPassword.getText().toString().trim();
//
//        if (!email.isEmpty() && !password.isEmpty()) {
//            formIsValid = true;
//        } else {
//            if (email.isEmpty()) {
//                txtEmail.setError(activity.getString(R.string.err_required));
//            }
//
//            if (password.isEmpty()) {
//                txtPassword.setError(activity.getString(R.string.err_required));
//            }
//        }
//
//        if (formIsValid) {
//            // Check to see email is in the correct format
//            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                txtEmail.setError(activity.getString(R.string.err_required_email_format));
//                formIsValid = false;
//            }
//
//            // Check for password at min length
//            if (password.length() < MIN_PASSWORD_LEN) {
//                txtPassword.setError(activity.getString(R.string.err_required_password_min));
//                formIsValid = false;
//            }
//        }
//
//        return formIsValid;
//    }
}