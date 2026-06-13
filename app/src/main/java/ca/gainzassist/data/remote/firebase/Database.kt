package ca.gainzassist.data.remote.firebase

import android.app.Activity
import android.content.Intent
import android.util.SparseArray
import ca.gainzassist.core.util.Misc.isMyServiceRunning
import ca.gainzassist.data.service.FirebaseService
import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.orhanobut.logger.Logger
import androidx.core.util.size

object Database {

    private const val EMAIL = "email"
    private const val WORKOUTS = "workouts"
    private const val SESSIONS = "sessions"
    private const val DEFAULT_WORKOUTS_PATH = "default_workouts"
    private const val USER_PATH = "users/%s"

    private var firebaseUser: FirebaseUser? = null
    private var userRef: DatabaseReference? = null
    private var userWorkoutsRef: DatabaseReference? = null

    private fun getUserRef(): DatabaseReference? {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (firebaseUser != null) {
            firebaseDatabase.getReference(String.format(USER_PATH, firebaseUser!!.uid))
        } else null
    }

    @JvmStatic
    fun getWorkoutsRef(): DatabaseReference? {
        userRef = getUserRef()
        return userRef?.child(WORKOUTS)
    }

    @JvmStatic
    fun getWorkoutSessionsRef(): DatabaseReference? {
        userRef = getUserRef()
        return userRef?.child(SESSIONS)
    }

    @JvmStatic
    fun setUserInfo(act: Activity) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            userRef = getUserRef()
            userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userShot: DataSnapshot) {
                    if (!userShot.hasChildren()) {
                        userRef?.child(EMAIL)?.setValue(firebaseUser?.email)
                        copyDefaultWorkoutsFirebase()
                    }
                    if (!isMyServiceRunning(act, FirebaseService::class.java)) {
                        act.startService(Intent(act, FirebaseService::class.java))
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) = Unit
            })
        }
    }

    private fun copyDefaultWorkoutsFirebase() {
        val defaultWorkoutsRef = FirebaseDatabase.getInstance().getReference(DEFAULT_WORKOUTS_PATH)
        userWorkoutsRef = getWorkoutsRef()
        if (userWorkoutsRef != null) {
            defaultWorkoutsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(defaultWorkoutsShot: DataSnapshot) {
                    userWorkoutsRef!!.setValue(defaultWorkoutsShot.value)
                }

                override fun onCancelled(databaseError: DatabaseError) = Unit
            })
        }
    }

    @JvmStatic
    fun addWorkoutFirebase(workout: Workout) {
        userWorkoutsRef = getWorkoutsRef()
        val workoutName = workout.name
        if (userWorkoutsRef != null && !workoutName.isNullOrBlank()) {
            userWorkoutsRef!!.child(workoutName).setValue(workout.toMap())
        } else {
            Logger.e(
                "Cannot add workout to Firebase. userWorkoutsRef=$userWorkoutsRef, workoutName=$workoutName"
            )
        }
    }

    @JvmStatic
    fun addWorkoutSessionFirebase(session: Session) {
        val userWorkoutSessionsRef = getWorkoutSessionsRef()
        userWorkoutSessionsRef?.child(session.timestamp.toString())?.setValue(session.toMap())
        updateWorkoutWeights(session.workoutName!!, session.avgWeights)
    }

    @JvmStatic
    fun updateWorkoutWeights(workoutName: String, newWeights: SparseArray<Float>) {
        userWorkoutsRef = getWorkoutsRef()
        if (userWorkoutsRef != null) {
            val workoutRef = userWorkoutsRef?.child(workoutName)
            for (i in 0 until newWeights.size) {
                workoutRef
                    ?.child("exercises")
                    ?.child(i.toString())
                    ?.child("weight")
                    ?.setValue(newWeights[i])
            }
        }
    }

    @JvmStatic
    fun deleteWorkoutFirebase(workoutName: String?) {
        userWorkoutsRef = getWorkoutsRef()
        if (workoutName != null) {
            userWorkoutsRef?.child(workoutName)?.removeValue()
        }
    }
}
