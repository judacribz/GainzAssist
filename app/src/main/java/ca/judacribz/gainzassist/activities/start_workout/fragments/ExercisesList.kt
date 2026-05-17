package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import com.orhanobut.logger.Logger;
import org.parceler.Parcels;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter.EXTRA_MAIN_EXERCISES;
import static ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET;

public class ExercisesList extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    Bundle bundle;

//    @BindView(R.id.ll_exercise_subtitle_insert) LinearLayout llExSubInsert;
    @BindView(R.id.ll_exercise_sets_insert) LinearLayout llExSetsInsert;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ExercisesList Constructor/Instance                                                        //
    // ######################################################################################### //
    public ExercisesList() {
        // Required empty public constructor
    }

    public static ExercisesList getInstance() {
        return new ExercisesList();
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
View view;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            return  view;
        }
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_exercises_list, container, false);
        ButterKnife.bind(this, view);

        if (bundle != null) {
            ArrayList<Exercise> exercises = Parcels.unwrap(bundle.getParcelable(EXTRA_MAIN_EXERCISES));
            if (exercises != null) {
                for (int i = exercises.size()-1; i >= 0; --i) {

                    act.displaySets(
                            MAIN_SET,
                            exercises.get(i),
//                            llExSubInsert,
                            llExSetsInsert
                    );
                }
            }
        }

       return view;
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
