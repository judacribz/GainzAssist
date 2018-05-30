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


       return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Workout workout = act.workout;
        ArrayList<Exercise> exercises = workout.getExercises();
        int exIndex     = exercises.size();

        Toast.makeText(act, "" + exIndex, Toast.LENGTH_SHORT).show();
//
//        // Set up the custom view (EquipmentView) to display the equipment. View added dynamically
//        // to trigger onDraw method
//        equipmentView = new EquipmentView(act);
//        equipmentView.setLayoutParams(
//                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT)
//        );
//        vgEquipment.addView(equipmentView, 0);
//
//        // Set up the first workout set information
//        setupNextSetInfo();
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
