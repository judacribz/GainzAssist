package ca.judacribz.gainzassist.util.firebase

import android.app.Activity
import android.content.Intent
import android.util.SparseArray
import ca.judacribz.gainzassist.background.FirebaseService
import ca.judacribz.gainzassist.models.Session
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.util.Misc.isMyServiceRunning
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

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
            if (userRef != null) {
                userRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userShot: DataSnapshot) {
                        if (!userShot.hasChildren()) {
                            userRef!!.child(EMAIL).setValue(firebaseUser!!.email)
                            copyDefaultWorkoutsFirebase()
                        }
                        if (!isMyServiceRunning(act, FirebaseService::class.java)) {
                            act.startService(Intent(act, FirebaseService::class.java))
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
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

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    @JvmStatic
    fun addWorkoutFirebase(workout: Workout) {
        userWorkoutsRef = getWorkoutsRef()
        if (userWorkoutsRef != null) {
            userWorkoutsRef!!.child(workout.name!!).setValue(workout.toMap())
        }
    }

    @JvmStatic
    fun addWorkoutSessionFirebase(session: Session) {
        val userWorkoutSessionsRef = getWorkoutSessionsRef()
        if (userWorkoutSessionsRef != null) {
            userWorkoutSessionsRef.child(session.timestamp.toString()).setValue(session.toMap())
        }
        updateWorkoutWeights(session.workoutName!!, session.avgWeights)
    }

    @JvmStatic
    fun updateWorkoutWeights(workoutName: String, newWeights: SparseArray<Float>) {
        userWorkoutsRef = getWorkoutsRef()
        if (userWorkoutsRef != null) {
            val workoutRef = userWorkoutsRef!!.child(workoutName)
            for (i in 0 until newWeights.size()) {
                workoutRef.child("exercises").child(i.toString()).child("weight").setValue(newWeights.get(i))
            }
        }
    }

    @JvmStatic
    fun deleteWorkoutFirebase(workoutName: String?) {
        userWorkoutsRef = getWorkoutsRef()
        if (userWorkoutsRef != null && workoutName != null) {
            userWorkoutsRef!!.child(workoutName).removeValue()
        }
    }
}
