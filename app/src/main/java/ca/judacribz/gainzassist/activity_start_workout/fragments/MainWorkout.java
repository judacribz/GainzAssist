package ca.judacribz.gainzassist.activity_start_workout.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.judacribz.gainzassist.R;

public class MainWorkout extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // MainWorkout Constructor/Instance                                                          //
    // ######################################################################################### //
    public MainWorkout() {
        // Required empty public constructor
    }

    public static MainWorkout newInstance() {
        return new MainWorkout();
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
        return inflater.inflate(R.layout.fragment_main_workout, container, false);
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
