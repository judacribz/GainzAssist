package ca.gainzassist.data.remote.firebase

import android.app.Activity
import android.content.Intent
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
        } else {
            null
        }
    }

    fun getWorkoutsRef(): DatabaseReference? {
        userRef = getUserRef()
        return userRef?.child(WORKOUTS)
    }

    fun getWorkoutSessionsRef(): DatabaseReference? {
        userRef = getUserRef()
        return userRef?.child(SESSIONS)
    }

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
                    if (!FirebaseService.isRunning) {
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

    fun addWorkoutSessionFirebase(session: Session) {
        val userWorkoutSessionsRef = getWorkoutSessionsRef()
        userWorkoutSessionsRef?.child(session.timestamp.toString())?.setValue(session.toMap())
        updateWorkoutWeights(session.workoutName!!, session.avgWeights)
    }

    fun updateWorkoutWeights(workoutName: String, newWeights: HashMap<Int, Float>) {
        userWorkoutsRef = getWorkoutsRef()
        if (userWorkoutsRef != null) {
            val workoutRef = userWorkoutsRef?.child(workoutName)
            for ((key, value) in newWeights) {
                workoutRef
                    ?.child("exercises")
                    ?.child(key.toString())
                    ?.child("weight")
                    ?.setValue(value)
            }
        }
    }

    fun deleteWorkoutFirebase(workoutName: String?) {
        userWorkoutsRef = getWorkoutsRef()
        if (workoutName != null) {
            userWorkoutsRef?.child(workoutName)?.removeValue()
        }
    }
}
