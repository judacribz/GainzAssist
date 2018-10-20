package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Workout;

//import static ca.judacribz.gainzassist.activities.start_workout.WorkoutPagerAdapter.EXTRA_WORKOUT;

public class ListExercises extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    LinearLayout vgSets, vgSubtitle;
    Bundle bundle;
    Workout workout;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ListExercises Constructor/Instance                                                        //
    // ######################################################################################### //
    public ListExercises() {
        // Required empty public constructor
    }

    public static ListExercises newInstance() {
        return new ListExercises();
    }
    // ######################################################################################### //


    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (StartWorkout) getActivity();
        bundle = getArguments();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view =  inflater.inflate(R.layout.fragment_curr_exercises, container, false);
       vgSubtitle = (LinearLayout) view.findViewById(R.id.ll_exercise_attr_insert);
       vgSets = (LinearLayout) view.findViewById(R.id.ll_exercise_sets_insert);
        if (bundle != null) {
//            workout = bundle.getParcelable(EXTRA_WORKOUT);
        }

        if (workout != null) {
            int i = 0;
            for (Exercise exercise : workout.getExercises()) {
                act.displaySets(200 + i, exercise.getName(), exercise.getSets(), vgSubtitle, vgSets);
            }
        }


       return view;
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
