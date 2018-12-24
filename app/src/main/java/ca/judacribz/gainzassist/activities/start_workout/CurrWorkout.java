package ca.judacribz.gainzassist.activities.start_workout;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Map;

import static ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET;
import static ca.judacribz.gainzassist.models.Exercise.SetsType.WARMUP_SET;
import static ca.judacribz.gainzassist.util.Calculations.getOneRepMax;
import static ca.judacribz.gainzassist.util.Misc.enablePrettyMapper;
import static ca.judacribz.gainzassist.util.Misc.readValue;
import static ca.judacribz.gainzassist.util.Misc.writeValueAsString;
import static ca.judacribz.gainzassist.util.Preferences.*;
import static ca.judacribz.gainzassist.constants.ExerciseConst.*;

public class CurrWorkout {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final CurrWorkout INST = new CurrWorkout();
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private Workout currWorkout;
    private Exercise currExercise;

    private ExerciseSet currExerciseSet;
    private float
            currWeight,
            currMinWeight = MIN_WEIGHT,
            currWeightChange = WEIGHT_CHANGE;

    private ArrayList<Exercise>
            currWarmups,
            currMains;

    private int
            set_i = -1,
            ex_i = -1,
            currReps,
            numWarmups,
            numMains;
    private long currRestTime;
    private boolean timerSet;

    private Session currSession = null;

    private Context context;
    // --------------------------------------------------------------------------------------------


    // Interfaces
    // --------------------------------------------------------------------------------------------
    private TimerListener timerListener;

    public interface TimerListener {
        void startTimer(long timeInMillis);
    }
    public void setTimerListener(TimerListener timerListener) {
        this.timerListener = timerListener;
        //TODO make deterministic
        if (!timerSet) {
            setTimer();
        }
    }

    private DataListener dataListener;
    public interface DataListener {
        void warmupsGenerated(ArrayList<Exercise> warmups);
    }
    void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }
    // --------------------------------------------------------------------------------------------


    // ######################################################################################### //
    // CurrWorkout Constructor/Instance                                                        //
    // ######################################################################################### //
    private CurrWorkout() {
    }

    public static CurrWorkout getInstance() {
        return INST;
    }
    // ######################################################################################### //

    public void setContext(Context context) {
        this.context = context;
    }

    void retrieveCurrWorkout(Workout workout) {
        setRetrievedWorkout(readValue(getIncompleteSessionPref(this.context, workout.getName())), workout);

        removeIncompleteSessionPref(this.context, workout.getName());
    }

    private void setRetrievedWorkout(Map<String, Object> map, Workout workout) {
        Map<String, Object> exsMap, exMap;
        Map<String, Object> setsMap, setMap;

        currWarmups = new ArrayList<>();
        this.currWorkout = workout;
        this.currSession = new Session(workout);

        this.ex_i = Integer.valueOf(String.valueOf(map.get(EXERCISE_INDEX)));
        //TODO remove
//        Logger.d("yooooo" + this.ex_i);
        this.set_i = Integer.valueOf(String.valueOf(map.get(SET_INDEX)));

        exsMap =  readValue(readValue(map.get(SESSION)).get(EXERCISES));
        for (Map.Entry<String, Object> exEntry : exsMap.entrySet()) {
            String exNum = exEntry.getKey();
            exMap = readValue(exEntry.getValue());

            //TODO test without using map
            Exercise exercise = workout.getExerciseFromIndex(Integer.valueOf(exNum));
            exercise.setSetsType(MAIN_SET);

            setsMap = readValue(readValue(exEntry.getValue()).get(SET_LIST));
            for (Map.Entry<String, Object> setEntry : setsMap.entrySet()) {
                setMap = readValue(setEntry.getValue());
                exercise.addSet(new ExerciseSet(
                        exercise,
                        Integer.valueOf(setEntry.getKey()),
                        Integer.valueOf(String.valueOf(setMap.get(REPS))),
                        Float.valueOf(String.valueOf(setMap.get(WEIGHT)))
                ), false);
            }

            this.currSession.addExercise(exercise);
        }

        genWarmups(workout.getExercises());

        enablePrettyMapper();
        Logger.d(writeValueAsString(this.currSession.sessionStateMap(
                ex_i,
                set_i
        )));
    }

    void setCurrWorkout(Workout workout) {
        this.currWorkout = workout;
        this.currSession = new Session(workout);
        genWarmups(workout.getExercises());
    }

    private void genWarmups(ArrayList<Exercise> exercises) {
        ArrayList<Exercise>
                allExs = new ArrayList<>(),
                warmups = new ArrayList<>();
        ArrayList<ExerciseSet> exerciseSets;
        Exercise warmup;

        float oneRepMax, minWeight, weight;
        String equip;

        setCurrMainExercises(exercises);

        for (Exercise ex: exercises) {
            equip = ex.getEquipment();
            minWeight = ex.getMinWeight();
            weight = ex.getAvgWeight();

            if (weight == minWeight) {
                allExs.add(ex);
                continue;
            }

            //Todo: use onerepmax
            oneRepMax = getOneRepMax(ex.getAvgReps(), ex.getAvgWeight());

            if (BARBELL.equals(equip)) {
                exerciseSets = genBBWarmups(ex);
            } else {
                exerciseSets = genWarmups(ex);
            }

            warmup = new Exercise(
                    ex.getExerciseNumber(),
                    ex.getName(),
                    ex.getType(),
                    ex.getEquipment(),
                    exerciseSets,
                    WARMUP_SET
            );
            warmups.add(warmup);
            allExs.add(warmup);
            allExs.add(ex);
        }

        setCurrWarmupExercises(warmups);
        setAllCurrExercises(allExs);
    }

    private ArrayList<ExerciseSet> genBBWarmups(Exercise ex) {
        ArrayList<ExerciseSet> exerciseSets = new ArrayList<>();
        int
                setNum = 0,
                reps = ex.getAvgReps();
        float
                minWeight = ex.getMinWeight(),
                weightChange = ex.getWeightChange(),
                weight = ex.getAvgWeight(),
                weightInc;

        float newWeight = minWeight;

        exerciseSets.add(new ExerciseSet(ex, setNum++, reps, newWeight));
        exerciseSets.add(new ExerciseSet(ex, setNum++, reps, newWeight));

        if (weight >= 405f) {
            weightInc = 90f;
            newWeight += 90f;
            while (newWeight <= 0.85f * weight) {
                if (newWeight >= 0.75f * weight) {
                    reps = ex.getReps()/4 + 1;
                }else if (newWeight > 0.65f * weight) {
                    reps = ex.getReps()/2 + 1;
                }

                exerciseSets.add(new ExerciseSet(ex, setNum++, reps, newWeight));
                newWeight += weightInc;
            }
        } else {
            newWeight += 50f;
            weightInc = 40f;
            while (newWeight <= 0.85f * weight) {

                if (newWeight >= 0.75f * weight) {
                    reps = ex.getReps()/4 + 1;
                }else if (newWeight > 0.65f * weight) {
                    reps = ex.getReps()/2 + 1;
                }

                exerciseSets.add(new ExerciseSet(ex, setNum++, reps, newWeight));
                newWeight += weightInc;
            }
        }

        reps = reps/2 + 1;
        do {
            if (weightInc < weightChange) {
                return exerciseSets;
            }

            if (newWeight < 0.91f * weight && newWeight > 0.85f * weight) {
                exerciseSets.add(new ExerciseSet(ex, setNum++, reps, newWeight));
                newWeight += weightInc;

                reps /=2;
            } else if (newWeight > 0.91f * weight) {
                newWeight -= weightInc;
                weightInc -= weightChange;
                newWeight += weightInc;
            }
        } while (true);
    }

    private ArrayList<ExerciseSet> genWarmups(Exercise ex) {
        ArrayList<ExerciseSet> exerciseSets = new ArrayList<>();
        int
                setNum = 0,
                reps = ex.getAvgReps();
        float
                minWeight = ex.getMinWeight(),
                weightChange = ex.getWeightChange(),
                weight = ex.getAvgWeight();

        float newWeight = minWeight;

        float diff = weight - minWeight;

        if (diff == 0f) {
            return exerciseSets;
        }

        int sets =  Math.min(5, (int)diff / (int)(weightChange * 2));
        sets += (int)weight / 100;
        float percInc = 0.91f/(float)sets;
        float perc = percInc;

        for (int i = 0; i < sets; i++) {
            newWeight = perc*weight;
            newWeight -= newWeight % (weightChange*2);
            if (newWeight >= 0.65f * weight) {
                reps = (reps/2 + 1);
            }

            exerciseSets.add(new ExerciseSet(
                    ex,
                    setNum++,
                    reps,
                    newWeight));
            perc += percInc;
        }

        return  exerciseSets;
    }

    private void setCurrMainExercises(ArrayList<Exercise> exercises) {
        this.currMains = exercises;
        this.numMains = exercises.size();
    }

    private void setCurrWarmupExercises(ArrayList<Exercise> warmups) {
        this.currWarmups = warmups;
        this.numWarmups = warmups.size();

        dataListener.warmupsGenerated(warmups);
    }

    private void setAllCurrExercises(ArrayList<Exercise> allExercises ) {
        this.currWorkout.setExercises(allExercises);

        if (this.ex_i == -1) {
            this.ex_i = 0;
        }
        setCurrExercise(this.currWorkout.getExerciseFromIndex(this.ex_i));
    }

    public boolean finishCurrSet() {
        this.set_i++;

        if (!getIsWarmup()) {
            addCurrSet();
        }

        // End of sets for an exercise
        if (atEndOfSets()) {
            this.set_i = 0;
            this.ex_i++;

            this.currSession.addExercise(this.currExercise);

            // End of all exercises for this workout session
            if (atEndOfExercises()) {
                this.currSession.setTimestamp(-1);
                ViewModelProviders.of((FragmentActivity) context)
                        .get(WorkoutViewModel.class)
                        .insertSession(this.currSession);

                removeSessPrefAndSetWorkout();
                resetIndices();

                return false;

            // Not end of all exercises, set next exercise
            } else {
                setCurrExercise(this.currWorkout.getExerciseFromIndex(this.ex_i));
            }

        // End of set, set next set from current exercise
        } else {
            setCurrExerciseSet(currExercise.getSet(this.set_i));
        }

        return true;
    }

    public void addCurrSet() {
        this.currExerciseSet.setReps(this.currReps);
        this.currExerciseSet.setWeight(this.currWeight);
        this.currExercise.addSet(this.currExerciseSet, true);
    }

    private boolean atEndOfSets() {
        return this.set_i >= this.currExercise.getNumSets();
    }

    private boolean atEndOfExercises() {
        return (this.ex_i >= this.currWorkout.getNumExercises());
    }

    public int getExInd() {
        return  this.ex_i;
    }

    public void setExInd(int ex_i) {
        this.ex_i = ex_i;
    }

    private void setCurrExercise(Exercise exercise) {
        if (this.set_i == -1) {
            this.set_i = 0;
        }

        this.currExercise = exercise;
        setCurrExerciseSet(this.currExercise.getSet(this.set_i));
    }

    private void setCurrExerciseSet(ExerciseSet exerciseSet) {
        this.currExerciseSet = exerciseSet;
        setCurrReps(this.currExerciseSet.getReps());
        this.currWeight = this.currExerciseSet.getWeight();
    }

    public ArrayList<Exercise> getWarmups() {
        return currWarmups;
    }

    public String getWorkoutName() {
        return currWorkout.getName();
    }

    public int getCurrNumExs() {
        return (getIsWarmup()) ? this.numWarmups : this.numMains;
    }

    public int getCurrExNum() {
        return ((this.currExercise.getSetsType() == WARMUP_SET)
                ? currWarmups.indexOf(currExercise)
                : currMains.indexOf(currExercise))
                + 1;
    }

    public String getCurrExName() {
        return currExercise.getName();
    }

    public String getCurrEquip() {
        return currExercise.getEquipment();
    }

    public int getCurrNumSets() {
        return currExercise.getNumSets();
    }

    public int getCurrSetNum() {
        return this.set_i + 1;
    }

    /* Reps------------------------------------------------------------------------------------- */
    public int getCurrReps() {
        return this.currReps;
    }

    public void incReps() {
        setCurrReps(++this.currReps);
    }

    public void decReps() {
        setCurrReps(--this.currReps);
    }

    public boolean setCurrReps(int reps) {
        this.currReps = reps;

        if (this.currExercise.getSetsType() == MAIN_SET) {
            setCurrRestTime();
        }

        return this.currReps == MIN_REPS;
    }

    private void setCurrRestTime() {
        this.currRestTime =  (this.currExerciseSet.getReps() <= 6) ? HEAVY_REST_TIME : LIGHT_REST_TIME;

        setTimer();
    }

    private void setTimer() {
        if (timerListener != null) {
            timerListener.startTimer(this.currRestTime);
            timerSet = true;
        } else {
            timerSet = false;
        }
    }

    public long getCurrRestTime() {
        return this.currRestTime;
    }
    /* Reps-end--------------------------------------------------------------------------------- */

    /* Weight----------------------------------------------------------------------------------- */
    public float getCurrWeight() {
        return this.currWeight;
    }

    public void incWeight() {
        setWeight(this.currWeight + this.currWeightChange);
    }

    public void decWeight() {
        setWeight(Math.max(this.currMinWeight, this.currWeight - this.currWeightChange));
    }

    public boolean setWeight(float weight) {
        this.currWeight = weight;

        return this.currWeight == this.currMinWeight;
    }

    public float getCurrMinWeight() {
        return currMinWeight;
    }
    /* Weight-end------------------------------------------------------------------------------- */


    public boolean getIsWarmup() {
        return WARMUP_SET.equals(this.currExercise.getSetsType());
    }

    public Session getCurrSession() {
        return this.currSession;
    }

    void resetIndices() {
        this.ex_i = -1;
        this.set_i = -1;
    }

    void saveSessionState() {

        if (!getIsWarmup()) {
            Toast.makeText(context, "IN CONDITION", Toast.LENGTH_SHORT).show();
            Exercise ex = this.currExercise;

            this.currSession.addExercise(ex);
        }

        enablePrettyMapper();
        String jsonStr = writeValueAsString(this.currSession.sessionStateMap(
                ex_i,
                set_i
        ));

        Logger.d("leave" + jsonStr);
        if (!jsonStr.isEmpty()) {
            addIncompleteSessionPref(
                    context,
                    getWorkoutName(),
                    jsonStr
            );
        }

    }

    private void removeSessPrefAndSetWorkout() {
        if (removeIncompleteWorkoutPref(context, currWorkout.getName())) {
            removeIncompleteSessionPref(context, currWorkout.getName());
        }
    }
}
