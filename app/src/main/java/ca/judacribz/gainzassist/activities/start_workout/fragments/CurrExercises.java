package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.judacribz.gainzassist.R;

public class CurrExercises extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view =  inflater.inflate(R.layout.fragment_main_exercises, container, false);


        return view;
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
