package ca.judacribz.gainzassist.background

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.widget.Toast
import ca.judacribz.gainzassist.models.db.WorkoutRepo
import ca.judacribz.gainzassist.util.Misc.extractSession
import ca.judacribz.gainzassist.util.Misc.extractWorkout
import ca.judacribz.gainzassist.util.firebase.Database.getWorkoutSessionsRef
import ca.judacribz.gainzassist.util.firebase.Database.getWorkoutsRef
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.orhanobut.logger.Logger

class FirebaseService : IntentService("FirebaseService") {

    private var workoutRepo: WorkoutRepo? = null

    override fun onCreate() {
        super.onCreate()
        workoutRepo = WorkoutRepo(application)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userWorkoutsRef = getWorkoutsRef()
        val userSessionRef = getWorkoutSessionsRef()

        if (userWorkoutsRef != null) {
            userWorkoutsRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(workoutShot: DataSnapshot, s: String?) {
                    workoutRepo!!.insertWorkout(extractWorkout(workoutShot))
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
        }

        if (userSessionRef != null) {
            userSessionRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(sessionShot: DataSnapshot, s: String?) {
                    val session = extractSession(sessionShot)
                    if (session != null) {
                        workoutRepo!!.insertSession(session, false)
                    }
                }

                override fun onChildChanged(sessionShot: DataSnapshot, s: String?) {}

                override fun onChildRemoved(sessionShot: DataSnapshot) {}

                override fun onChildMoved(sessionShot: DataSnapshot, s: String?) {}

                override fun onCancelled(databaseError: DatabaseError) {
                    Logger.d("FIREBASE DB SESSION ERROR: " + databaseError.message)
                }
            })
        }

        return Service.START_STICKY
    }

    override fun onHandleIntent(intent: Intent?) {}
}
