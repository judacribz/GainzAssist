package ca.judacribz.gainzassist.util.firebase

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.authentication.Login
import ca.judacribz.gainzassist.background.FirebaseService
import ca.judacribz.gainzassist.models.WorkoutHelper
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser

object Authentication {
    // Constants
    // --------------------------------------------------------------------------------------------
    private val mAuth = FirebaseAuth.getInstance()
    private const val MIN_PASSWORD_LEN = 6
    const val RC_SIGN_IN = 9001

    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    private var userCreated = false

    // --------------------------------------------------------------------------------------------
    /* Wrapper function to create an user in Firebase */
    fun createUser(
        act: Activity,
        email: String?,
        password: String?,
        signInClient: GoogleSignInClient
    ) {
        mAuth.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(act) { task -> handleOnComplete(act, signInClient, task) }
    }

    /* Wrapper function to log in an user using a firebase credential */
    fun signIn(act: Activity, cred: AuthCredential?, signInClient: GoogleSignInClient) {
        mAuth.signInWithCredential(cred!!)
            .addOnCompleteListener(act) { task -> handleOnComplete(act, signInClient, task) }
    }

    private fun handleOnComplete(
        act: Activity,
        signInClient: GoogleSignInClient,
         task: Task<AuthResult>
    ) {
        val msg: String

        // Sign up success
        if (task.isSuccessful) {
            userCreated = true
            msg = act.getString(R.string.sign_up_success)

            // Sign up fail
        } else {
            userCreated = false
            msg = getExceptionMsg(act, task.exception)
            (act as Login).loginFail()
            if (msg == act.getString(R.string.txt_email_registered)) {
                act.linkGoogle = true
                act.googleLogin()
            }
        }
        Toast.makeText(act, msg, Toast.LENGTH_SHORT).show()
    }

    fun linkUser(act: Activity?, cred: AuthCredential?, currUser: FirebaseUser) {
        currUser.linkWithCredential(cred!!)
            .addOnCompleteListener(act!!) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(act, "Linked!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        act,
                        "linkWithCredential:failure" + task.exception,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    //    public static void facebookSignIn(Activity act) {
    //        LoginManager.getInstance().logInWithReadPermissions(
    //                act,
    //                Arrays.asList("email", "public_profile")
    //        );
    //    }
    /* Wrapper function to sign out user */
    fun signOut(act: Activity, signInClient: GoogleSignInClient) {
        FirebaseAuth.getInstance().signOut()
        signInClient.signOut().addOnCompleteListener(
            act
        ) { }
        act.stopService(Intent(act, FirebaseService::class.java))
        WorkoutHelper(act).deleteAllWorkouts()
    }

    /* Used by sign in and sign up to firebase to handle exception if they fail */
    private fun getExceptionMsg(act: Activity, taskEx: Exception?): String {
        var msg = ""
        if (taskEx != null) {
            msg = try {
                throw taskEx

                // No internet connection
            } catch (ex: FirebaseNetworkException) {
                act.getString(R.string.txt_network_needed)

                // Password does not meet minimum requirements
            } catch (ex: FirebaseAuthWeakPasswordException) {
                act.getString(R.string.err_invalid_password)

                // Email is already registered
            } catch (ex: FirebaseAuthUserCollisionException) {
                act.getString(R.string.txt_email_registered)

                // Email format is invalid
            } catch (ex: FirebaseAuthInvalidCredentialsException) {
                act.getString(R.string.err_invalid_email)

                // Other exceptions
            } catch (ex: Exception) {
                ex.toString()
            }
        }
        return msg
    } //    /* Validates login and sign up forms using email and password combination */
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