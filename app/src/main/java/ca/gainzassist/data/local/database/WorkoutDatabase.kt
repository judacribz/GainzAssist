package ca.gainzassist.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.ExerciseSet
import ca.gainzassist.domain.model.Session
import ca.gainzassist.domain.model.Workout

@Database(
    entities = [Workout::class, Exercise::class, ExerciseSet::class, Session::class],
    version = 1,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun sessionDao(): SessionDao

    companion object {
        private const val DATABASE_NAME = "workout_database"

        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        private val sRoomDatabaseCallback = object : Callback() {
            /* no-op */
        }

        @JvmStatic
        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: getDatabaseBuilder(context)
                    .addCallback(sRoomDatabaseCallback)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private fun getDatabaseBuilder(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            WorkoutDatabase::class.java,
            DATABASE_NAME
        )
    }
}
