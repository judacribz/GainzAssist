package ca.judacribz.gainzassist.util.firebase

import android.app.Activity
import android.content.Intent
import android.util.SparseArray
import ca.judacribz.gainzassist.background.FirebaseService
import ca.judacribz.gainzassist.models.Session
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.util.Misc
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object Database {
    // Constants
    // --------------------------------------------------------------------------------------------
    private const val EMAIL = "email"
    private const val USERNAME = "username"
    private const val WORKOUTS = "workouts"
    private const val SESSIONS = "sessions"
    private const val SETS = "sets"
    private const val DEFAULT_WORKOUTS_PATH = "default_workouts"
    private const val USER_PATH = "users/%s"
    private var firebaseUser: FirebaseUser? = null
    private var userRef: DatabaseReference? = null
    private var userWorkoutsRef: DatabaseReference? = null

    // --------------------------------------------------------------------------------------------
    //TODO change to getLive ref for a specific user when friends/chatting added
    /* Gets firebase db reference for 'users/<uid>/' */
    private fun getUserRef(): DatabaseReference? {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (firebaseUser != null) firebaseDatabase.getReference(
            String.format(
                USER_PATH,
                firebaseUser!!.uid
            )
        ) else null
    }

    @JvmStatic
    val workoutsRef: DatabaseReference?
        /* Gets firebase db reference for 'users/<uid>/workouts/' */
        get() {
            userRef = getUserRef()
            return if (userRef != null) userRef!!.child(WORKOUTS) else null
        }
    @JvmStatic
    val workoutSessionsRef: DatabaseReference?
        /* Gets firebase db reference for 'users/<uid>/sessions/' */
        get() {
            userRef = getUserRef()
            return if (userRef != null) userRef!!.child(SESSIONS) else null
        }

    /* Sets the user email in firebase db under 'users/<uid>/email' */
    fun setUserInfo(act: Activity) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        // FIREBASE: add user data
        if (firebaseUser != null) {
            userRef = getUserRef()
            if (userRef != null) {
                userRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange( userShot: DataSnapshot) {

                        // If newly added user
                        if (!userShot.hasChildren()) {
                            userRef!!.child(EMAIL).setValue(
                                firebaseUser!!.email
                            )

                            // Copy default workouts from 'default_workouts/' to  'user/<uid>/workouts/'
                            copyDefaultWorkoutsFirebase()
                        }
                        if (!Misc.isMyServiceRunning(act, FirebaseService::class.java)) {
                            act.startService(Intent(act, FirebaseService::class.java))
                        }
                    }

                    override fun onCancelled( databaseError: DatabaseError) {}
                })
            }
        }
    }

    /* Gets workouts from firebase db under 'default_workouts/' and adds it to
     * 'users/<uid>/workouts/' */
    private fun copyDefaultWorkoutsFirebase() {
        val defaultWorkoutsRef = FirebaseDatabase.getInstance().getReference(DEFAULT_WORKOUTS_PATH)
        userWorkoutsRef = workoutsRef
        if (userWorkoutsRef != null) {
            defaultWorkoutsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange( defaultWorkoutsShot: DataSnapshot) {
                    userWorkoutsRef!!.setValue(defaultWorkoutsShot.value)
                }

                override fun onCancelled( databaseError: DatabaseError) {}
            })
        }
    }

    /* Adds a workout under "users/<uid>/workouts/" */
    fun addWorkoutFirebase(workout: Workout) {
        userWorkoutsRef = workoutsRef
        if (userWorkoutsRef != null) {
            workout.name?.let { userWorkoutsRef!!.child(it).setValue(workout.toMap()) }
        }
    }

    @JvmStatic
    fun addWorkoutSessionFirebase(session: Session) {
        val userWorkoutSessionsRef = workoutSessionsRef
        userWorkoutSessionsRef?.child(session.timestamp.toString())?.setValue(session.toMap())
        updateWorkoutWeights(session.workoutName, session.avgWeights)
    }

    fun updateWorkoutWeights(workoutName: String?, newWeights: SparseArray<Float?>) {
        userWorkoutsRef = workoutsRef
        if (userWorkoutsRef != null) {
            val workoutRef = userWorkoutsRef!!.child(
                workoutName!!
            )
            for (i in 0 until newWeights.size()) {
                workoutRef.child("exercises").child(i.toString()).child("weight").setValue(
                    newWeights[i]
                )
            }
        }
    }

    @JvmStatic
    fun deleteWorkoutFirebase(workoutName: String?) {
        userWorkoutsRef = workoutsRef
        if (userWorkoutsRef != null) {
            userWorkoutsRef!!.child(workoutName!!).removeValue()
        }
    }
}