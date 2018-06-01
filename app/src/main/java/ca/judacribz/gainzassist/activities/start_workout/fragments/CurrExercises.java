package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Workout;

public class CurrExercises extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    ViewGroup vgSets, vgSubtitle;

    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // CurrExercises Constructor/Instance                                                        //
    // ######################################################################################### //
    public CurrExercises() {
        // Required empty public constructor
    }

    public static CurrExercises newInstance() {
        return new CurrExercises();
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
       View view =  inflater.inflate(R.layout.fragment_curr_exercises, container, false);
       vgSubtitle = (ViewGroup) view.findViewById(R.id.ll_exercise_attr_insert);
       vgSets = (ViewGroup) view.findViewById(R.id.ll_exercise_sets_insert);

       return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Workout workout = act.workout;
        ArrayList<Exercise> exercises = workout.getExercises();
        int exIndex = exercises.size();

//        Toast.makeText(act, "" + exIndex, Toast.LENGTH_SHORT).show();

        Exercise exercise;
        for (int i = 0; i < exercises.size(); i++) {
            exercise = exercises.get(i);

            act.displaySets(100 + i, exercise.getName(), exercise.getSets(), vgSubtitle, vgSets);

        }
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
