package ca.gainzassist.util.firebase

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import ca.gainzassist.R
import ca.gainzassist.activities.authentication.Login
import ca.gainzassist.background.FirebaseService
import ca.gainzassist.models.db.WorkoutViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.FragmentActivity

object Authentication {

    private val mAuth = FirebaseAuth.getInstance()
    const val RC_SIGN_IN = 9001
    private var userCreated = false

    @JvmStatic
    val currentUser: FirebaseUser?
        get() = mAuth.currentUser

    @JvmStatic
    fun createUser(act: Activity, email: String, password: String, signInClient: GoogleSignInClient) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(act) { task -> handleOnComplete(act, signInClient, task) }
    }

    @JvmStatic
    fun signIn(act: Activity, cred: AuthCredential, signInClient: GoogleSignInClient) {
        mAuth.signInWithCredential(cred)
            .addOnCompleteListener(act) { task -> handleOnComplete(act, signInClient, task) }
    }

    private fun handleOnComplete(
        act: Activity,
        signInClient: GoogleSignInClient,
        task: com.google.android.gms.tasks.Task<AuthResult>
    ) {
        val msg: String
        if (task.isSuccessful) {
            userCreated = true
            msg = act.getString(R.string.sign_up_success)
        } else {
            userCreated = false
            val ex = task.exception
            if (ex != null) {
                Log.e(Authentication::class.java.simpleName, "Firebase Auth Error: " + ex.javaClass.name)
                Log.e(Authentication::class.java.simpleName, "Message: " + ex.message)
            }
            msg = getExceptionMsg(act, ex)
            (act as Login).loginFail()
            if (msg == act.getString(R.string.txt_email_registered)) {
                act.linkGoogle = true
                act.googleLogin()
            }
        }
        Toast.makeText(act, msg, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun linkUser(act: Activity, cred: AuthCredential, currUser: FirebaseUser) {
        currUser.linkWithCredential(cred)
            .addOnCompleteListener(act) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(act, "Linked!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(act, "linkWithCredential:failure" + task.exception, Toast.LENGTH_SHORT).show()
                }
            }
    }

    @JvmStatic
    fun signOut(act: Activity, signInClient: GoogleSignInClient) {
        FirebaseAuth.getInstance().signOut()
        signInClient.signOut().addOnCompleteListener(act) {}
        act.stopService(Intent(act, FirebaseService::class.java))
        if (act is FragmentActivity) {
            ViewModelProvider(act).get(WorkoutViewModel::class.java).deleteAllWorkouts()
        }
    }

    private fun getExceptionMsg(act: Activity, taskEx: Exception?): String {
        var msg = ""
        if (taskEx != null) {
            try {
                throw taskEx
            } catch (ex: FirebaseNetworkException) {
                msg = act.getString(R.string.txt_network_needed)
            } catch (ex: FirebaseAuthWeakPasswordException) {
                msg = act.getString(R.string.err_invalid_password)
            } catch (ex: FirebaseAuthUserCollisionException) {
                msg = act.getString(R.string.txt_email_registered)
            } catch (ex: FirebaseAuthInvalidCredentialsException) {
                msg = act.getString(R.string.err_invalid_email)
            } catch (ex: Exception) {
                msg = "Login failed. Check Firebase configuration."
            }
        }
        return msg
    }
}
