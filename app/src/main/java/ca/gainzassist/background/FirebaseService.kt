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
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FirebaseService : IntentService("FirebaseService"), KoinComponent {

    private val workoutRepository: WorkoutRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Deprecated("Deprecated in Java")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userWorkoutsRef = getWorkoutsRef()
        val userSessionRef = getWorkoutSessionsRef()

        userWorkoutsRef?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(workoutShot: DataSnapshot, s: String?) {
                val workout = extractWorkout(workoutShot)
                serviceScope.launch {
                    workoutRepository.insertWorkout(workout, syncToFirebase = false)
                }
            }

            override fun onChildChanged(workoutShot: DataSnapshot, s: String?) {
                // Update logic if needed
            }

            override fun onChildRemoved(workoutShot: DataSnapshot) {
                Toast.makeText(this@FirebaseService, "Deleted " + workoutShot.key, Toast.LENGTH_SHORT).show()
                // Delete logic if needed
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {
                Logger.d("FIREBASE DB WORKOUT ERROR: " + databaseError.message)
            }
        })

        userSessionRef?.addChildEventListener(object : ChildEventListener {
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
        })

        return START_STICKY
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {}

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
