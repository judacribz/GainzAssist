package ca.gainzassist.core.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import ca.gainzassist.core.constants.ExerciseConst.EXERCISES
import ca.gainzassist.core.constants.ExerciseConst.SET_LIST
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.ExerciseSet
import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.model.Workout
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.google.firebase.database.DataSnapshot
import com.orhanobut.logger.Logger
import java.io.IOException
import java.util.Date

object Misc {

    private val mapper = ObjectMapper()

    @JvmStatic
    @Suppress("deprecation")
    fun isMyServiceRunning(act: Activity, serviceClass: Class<*>): Boolean {
        val manager = act.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        if (manager != null) {
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun extractWorkout(workoutShot: DataSnapshot): Workout {
        val exercises = ArrayList<Exercise>()
        var exercise: Exercise?
        val idObj = workoutShot.child("id").value
        val workoutId = idObj?.toString()?.toLongOrNull()
            ?: workoutShot.key?.hashCode()?.toLong()?.let { kotlin.math.abs(it) }
            ?: Date().time
        for (exerciseShot in workoutShot.child("exercises").children) {
            if (exerciseShot != null) {
                exercise = exerciseShot.getValue(Exercise::class.java)
                if (exercise != null) {
                    exercise.exerciseNumber = exerciseShot.key!!.toInt()
                    exercise.workoutId = workoutId
                    Logger.d("WORKOUT ID $workoutId")
                    exercises.add(exercise)
                }
            }
        }
        val workout = Workout(workoutShot.key, exercises)
        workout.id = workoutId
        return workout
    }

    @JvmStatic
    fun extractSession(sessionShot: DataSnapshot): Session? {
        val session = sessionShot.getValue(Session::class.java)
        if (session != null) {
            val timestamp = session.timestamp
            var exercise: Exercise?
            var set: ExerciseSet?
            session.timestamp = sessionShot.key!!.toLong()
            var exerciseName: String?
            var exerciseId: Long
            for (exerciseShot in sessionShot.child(EXERCISES).children) {
                exercise = exerciseShot.getValue(Exercise::class.java)
                if (exercise != null) {
                    exerciseId = exercise.id
                    exerciseName = exercise.name
                    exercise.exerciseNumber = exerciseShot.key!!.toInt()
                    for (setShot in exerciseShot.child(SET_LIST).children) {
                        set = setShot.getValue(ExerciseSet::class.java)
                        if (set != null) {
                            set.setNumber = setShot.key!!.toInt()
                            set.exerciseId = exerciseId
                            set.exerciseName = exerciseName
                            set.sessionId = timestamp
                            exercise.addSet(set, false)
                        }
                    }
                    session.addExercise(exercise)
                }
            }
        }
        return session
    }

    @JvmStatic
    fun exerciseToMap(exercises: ArrayList<Exercise>): Map<String, Any?> {
        val exs = HashMap<String, Any?>()
        for (exercise in exercises) {
            exs[exercise.exerciseNumber.toString()] = exercise.toMap()
        }
        return exs
    }

    @JvmStatic
    fun enablePrettyMapper() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
    }

    @JvmStatic
    fun readValue(childObj: Any?): Map<String, Any?> {
        return readValue(writeValueAsString(childObj))
    }

    @JvmStatic
    fun writeValueAsString(`object`: Any?): String {
        var jsonStr = ""
        try {
            jsonStr = mapper.writeValueAsString(`object`)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
        return jsonStr
    }

    @JvmStatic
    fun readValue(childStr: String?): Map<String, Any?> {
        var childMap: Map<String, Any?> = HashMap()
        try {
            childMap = mapper.readValue(
                childStr,
                object : TypeReference<HashMap<String, Any?>?>() {}
            ).orEmpty()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        return childMap
    }

    @JvmStatic
    fun shrinkTo(list: MutableList<*>?, newSize: Int) {
        list?.let {
            while (it.size > newSize) {
                it.removeAt(it.size - 1)
            }
        }
    }
}
