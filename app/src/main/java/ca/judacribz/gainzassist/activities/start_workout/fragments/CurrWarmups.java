package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;

import static ca.judacribz.gainzassist.util.Calculations.*;

public class CurrWarmups extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    LinearLayout vgSets, vgSubtitle;
    ArrayList<Exercise> warmups;

    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // CurrWarmups Constructor/Instance                                                        //
    // ######################################################################################### //
    public CurrWarmups() {
        // Required empty public constructor
    }

    public static CurrWarmups newInstance() {
        return new CurrWarmups();
    }
    // ######################################################################################### //

    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (StartWorkout) getActivity();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_curr_warmups, container, false);
        vgSubtitle = (LinearLayout) view.findViewById(R.id.ll_exercise_attr_insert);
        vgSets = (LinearLayout) view.findViewById(R.id.ll_exercise_sets_insert);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Workout workout = act.workout;
        ArrayList<Exercise> exercises = workout.getExercises();
        warmups = new ArrayList<>();

        float oneRepMax;
        Exercise exercise;
        for (int i = 0; i < exercises.size(); i++) {
            exercise = exercises.get(i);
            oneRepMax = getOneRepMax(exercise.getAvgReps(), exercise.getAvgWeight());

            int reps = exercise.getAvgReps();
            ArrayList<Set> sets = new ArrayList<>();
            int ind = 1;
            while (reps > 0) {
               sets.add(new Set(ind++, reps, 0.0f));
               reps -= 2;
            }

            Set set;
            float percent = 0.5f;
            float increments = 0.3f / (float) (sets.size() - 1);
            float weight = exercise.getAvgWeight();
            for (int j = 0; j < sets.size(); j++) {
                set = sets.get(j);
                set.setWeight(percent * weight);
                sets.set(j, set);
                percent += increments;
            }

            warmups.add(new Exercise(exercise.getName(), exercise.getType(), exercise.getEquipment(), sets));
        }

        for (int i = 0; i < warmups.size(); i++) {
            exercise = warmups.get(i);

            act.displaySets(100 + i, exercise.getName(), exercise.getSets(), vgSubtitle, vgSets);
        }
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
