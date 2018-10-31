package ca.judacribz.gainzassist.models;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRepo {
    private WorkoutDao workoutDao;
    private LiveData<List<Workout>> workouts;

    WorkoutRepo(Application app) {
        WorkoutDatabase db = WorkoutDatabase.getDatabase(app);
        workoutDao = db.workoutDao();
        workouts = workoutDao.getAllWorkouts();
    }

    LiveData<List<Workout>> getAllWorkouts() {
        return workouts;
    }

    public void insert (Workout workout) {
        new insertAsyncTask(workoutDao).execute(workout);
    }
    public void deleteAll() {
        new deleteAsyncTask(workoutDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<Workout, Void, Void> {

        private WorkoutDao mAsyncTaskDao;

        insertAsyncTask(WorkoutDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Workout... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Void, Void, Void> {

        private WorkoutDao mAsyncTaskDao;

        deleteAsyncTask(WorkoutDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

}
