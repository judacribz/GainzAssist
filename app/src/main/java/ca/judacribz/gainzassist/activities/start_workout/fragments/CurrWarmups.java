package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.judacribz.gainzassist.R;

public class CurrWarmups extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // CurrWarmups Constructor/Instance                                                      //
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_warmup_exercises, container, false);
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
