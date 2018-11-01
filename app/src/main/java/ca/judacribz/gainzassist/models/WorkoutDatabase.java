package ca.judacribz.gainzassist.models;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.*;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;

@Database(entities = {Workout.class, Exercise.class, Set.class},
          version = 1,
          exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {

    public abstract WorkoutDao workoutDao();
    public abstract ExerciseDao exerciseDao();
    public abstract SetDao setDao();

    private static volatile WorkoutDatabase INSTANCE;

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                }
            };

    static WorkoutDatabase getDatabase(final Context context) {
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



    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final WorkoutDao workoutDao;

        PopulateDbAsync(WorkoutDatabase db) {
            workoutDao = db.workoutDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
//            workoutDao.deleteAllWorkouts();
//            workoutDao.insert(new Workout("yy", null));

            return null;
        }
    }

}
