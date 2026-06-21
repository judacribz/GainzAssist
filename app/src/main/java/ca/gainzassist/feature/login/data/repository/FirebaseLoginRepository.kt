package ca.gainzassist.feature.login.data.repository

import ca.gainzassist.feature.login.domain.repository.LoginRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseLoginRepository : LoginRepository {

    private val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    override suspend fun loginWithEmail(email: String, password: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) continuation.resume(Result.success(Unit))
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(
                                Result.failure(task.exception ?: Exception("Authentication failed"))
                            )
                        }
                    }
                }
        }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) continuation.resume(Result.success(Unit))
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(
                                Result.failure(task.exception ?: Exception("Registration failed"))
                            )
                        }
                    }
                }
        }

    override suspend fun signOut(): Result<Unit> {
        firebaseAuth.signOut()
        return Result.success(Unit)
    }
}
