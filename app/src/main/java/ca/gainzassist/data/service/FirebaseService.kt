package ca.gainzassist.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import ca.gainzassist.core.util.Misc.extractSession
import ca.gainzassist.core.util.Misc.extractWorkout
import ca.gainzassist.data.remote.firebase.Database.getWorkoutSessionsRef
import ca.gainzassist.data.remote.firebase.Database.getWorkoutsRef
import ca.gainzassist.domain.repository.WorkoutRepository
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

class FirebaseService : Service(), KoinComponent {

    companion object {
        var isRunning = false
    }

    private val workoutRepository: WorkoutRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var userWorkoutsRef: DatabaseReference? = null
    private var userSessionRef: DatabaseReference? = null

    private var workoutListener: ChildEventListener? = null
    private var sessionListener: ChildEventListener? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        if (workoutListener == null) {
            getWorkoutsRef()?.let { ref ->
                val listener = createWorkoutListener()
                userWorkoutsRef = ref
                workoutListener = listener
                ref.addChildEventListener(listener)
            }
        }

        if (sessionListener == null) {
            getWorkoutSessionsRef()?.let { ref ->
                val listener = createSessionListener()
                userSessionRef = ref
                sessionListener = listener
                ref.addChildEventListener(listener)
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

        override fun onChildChanged(workoutShot: DataSnapshot, s: String?) {} // Empty on purpose

        override fun onChildRemoved(workoutShot: DataSnapshot) {
            Toast.makeText(this@FirebaseService, "Deleted " + workoutShot.key, Toast.LENGTH_SHORT).show()
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {} // Empty on purpose

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

        override fun onChildChanged(sessionShot: DataSnapshot, s: String?) {} // Empty on purpose

        override fun onChildRemoved(sessionShot: DataSnapshot) {} // Empty on purpose

        override fun onChildMoved(sessionShot: DataSnapshot, s: String?) {} // Empty on purpose

        override fun onCancelled(databaseError: DatabaseError) {
            Logger.d("FIREBASE DB SESSION ERROR: " + databaseError.message)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
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
