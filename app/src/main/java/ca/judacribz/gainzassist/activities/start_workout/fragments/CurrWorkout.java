package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.Exercise;

public class CurrWorkout extends Fragment{

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    ViewGroup vgEquipment;
    EquipmentView equipmentView;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // CurrWorkout Constructor/Instance                                                          //
    // ######################################################################################### //
    public CurrWorkout() {
        // Required empty public constructor
    }

    public static CurrWorkout newInstance() {
        return new CurrWorkout();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_curr_workout, container, false);

        // View to insert EquipmentView
        vgEquipment = (ViewGroup) view.findViewById(R.id.rlEquipmentDisplay);

        // Set up the custom view (EquipmentView) to display the equipment. View added dynamically
        // to trigger onDraw method
        equipmentView = new EquipmentView(act);
        equipmentView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        vgEquipment.addView(equipmentView, 0);

//TODO: make deterministic
        equipmentView.post(new Runnable() {
            @Override
            public void run() {
                equipmentView.setup(315, getString(R.string.barbell));
            }
        });


        return view;
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
