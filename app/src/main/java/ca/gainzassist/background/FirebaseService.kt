package ca.gainzassist.background

import android.app.IntentService
import android.content.Intent
import android.widget.Toast
import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.util.Misc.extractSession
import ca.gainzassist.util.Misc.extractWorkout
import ca.gainzassist.util.firebase.Database.getWorkoutSessionsRef
import ca.gainzassist.util.firebase.Database.getWorkoutsRef
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("DEPRECATION")
class FirebaseService : IntentService("FirebaseService"), KoinComponent {

    private val workoutRepository: WorkoutRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var userWorkoutsRef: DatabaseReference? = null
    private var userSessionRef: DatabaseReference? = null

    private var workoutListener: ChildEventListener? = null
    private var sessionListener: ChildEventListener? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        userWorkoutsRef = getWorkoutsRef()
        userSessionRef = getWorkoutSessionsRef()

        if (workoutListener == null) {
            workoutListener = createWorkoutListener().also { listener ->
                userWorkoutsRef?.addChildEventListener(listener)
            }
        }

        if (sessionListener == null) {
            sessionListener = createSessionListener().also { listener ->
                userSessionRef?.addChildEventListener(listener)
            }
        }

        return START_STICKY
    }

    private fun createWorkoutListener(): ChildEventListener = object : ChildEventListener {
        override fun onChildAdded(workoutShot: DataSnapshot, s: String?) {
            val workout = extractWorkout(workoutShot)
            serviceScope.launch {
                workoutRepository.insertWorkout(workout, syncToFirebase = false)
            }
        }

        override fun onChildChanged(workoutShot: DataSnapshot, s: String?) {}

        override fun onChildRemoved(workoutShot: DataSnapshot) {
            Toast.makeText(this@FirebaseService, "Deleted " + workoutShot.key, Toast.LENGTH_SHORT).show()
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {
            Logger.d("FIREBASE DB WORKOUT ERROR: " + databaseError.message)
        }
    }

    private fun createSessionListener(): ChildEventListener = object : ChildEventListener {
        override fun onChildAdded(sessionShot: DataSnapshot, s: String?) {
            val session = extractSession(sessionShot)
            if (session != null) {
                serviceScope.launch {
                    workoutRepository.insertCompletedSession(session, syncToFirebase = false)
                }
            }
        }

        override fun onChildChanged(sessionShot: DataSnapshot, s: String?) {}

        override fun onChildRemoved(sessionShot: DataSnapshot) {}

        override fun onChildMoved(sessionShot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {
            Logger.d("FIREBASE DB SESSION ERROR: " + databaseError.message)
        }
    }

    override fun onHandleIntent(intent: Intent?) {}

    override fun onDestroy() {
        workoutListener?.let { listener ->
            userWorkoutsRef?.removeEventListener(listener)
        }
        sessionListener?.let { listener ->
            userSessionRef?.removeEventListener(listener)
        }
        workoutListener = null
        sessionListener = null
        userWorkoutsRef = null
        userSessionRef = null

        serviceScope.cancel()
        super.onDestroy()
    }
}
