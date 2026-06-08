package ca.gainzassist.models.db;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.*;
import android.content.Context;
import androidx.annotation.NonNull;
import ca.gainzassist.models.Exercise;
import ca.gainzassist.models.Session;
import ca.gainzassist.models.ExerciseSet;
import ca.gainzassist.models.Workout;

@Database(entities = {Workout.class, Exercise.class, ExerciseSet.class, Session.class},
          version = 1,
          exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {

    public abstract WorkoutDao workoutDao();
    public abstract ExerciseDao exerciseDao();
    public abstract SetDao setDao();
    public abstract SessionDao sessionDao();

    private static volatile WorkoutDatabase INSTANCE;

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                }
            };

    public static WorkoutDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WorkoutDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WorkoutDatabase.class, "workout_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
