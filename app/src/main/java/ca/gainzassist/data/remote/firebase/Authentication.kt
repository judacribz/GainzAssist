package ca.gainzassist.data.remote.firebase

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import ca.gainzassist.R
import ca.gainzassist.data.service.FirebaseService
import ca.gainzassist.feature.login.presentation.screen.LoginActivity
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

    private val mAuth = FirebaseAuth.getInstance()
    const val RC_SIGN_IN = 9001
    private var userCreated = false

    @JvmStatic
    fun createUser(act: Activity, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(act) { task -> handleOnComplete(act, task) }
    }

    @JvmStatic
    fun signIn(act: Activity, cred: AuthCredential) {
        mAuth.signInWithCredential(cred)
            .addOnCompleteListener(act) { task ->
                handleOnComplete(act, task)
            }
    }

    private fun handleOnComplete(
        act: Activity,
        task: Task<AuthResult>
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
            (act as LoginActivity).loginFail()
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

    fun signOut(act: Activity) {
        FirebaseAuth.getInstance().signOut()
        act.stopService(Intent(act, FirebaseService::class.java))
    }

    private fun getExceptionMsg(act: Activity, taskEx: Exception?): String {
        var msg = ""
        if (taskEx != null) {
            msg = try {
                throw taskEx
            } catch (_: FirebaseNetworkException) {
                act.getString(R.string.txt_network_needed)
            } catch (_: FirebaseAuthWeakPasswordException) {
                act.getString(R.string.err_invalid_password)
            } catch (_: FirebaseAuthUserCollisionException) {
                act.getString(R.string.txt_email_registered)
            } catch (_: FirebaseAuthInvalidCredentialsException) {
                act.getString(R.string.err_invalid_email)
            } catch (_: Exception) {
                "Login failed. Check Firebase configuration."
            }
        }
        return msg
    }
}
