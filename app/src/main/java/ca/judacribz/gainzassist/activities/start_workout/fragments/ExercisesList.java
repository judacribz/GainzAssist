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
import ca.judacribz.gainzassist.models.Workout;
import org.parceler.Parcels;

import static ca.judacribz.gainzassist.activities.start_workout.WorkoutPagerAdapter.EXTRA_WORKOUT;

public class ExercisesList extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkout act;
    Bundle bundle;

    @BindView(R.id.ll_exercise_subtitle_insert) LinearLayout llExSubInsert;
    @BindView(R.id.ll_exercise_sets_insert) LinearLayout llExSetsInsert;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ExercisesList Constructor/Instance                                                        //
    // ######################################################################################### //
    public ExercisesList() {
        // Required empty public constructor
    }

    public static ExercisesList newInstance() {
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_exercises_list, container, false);
        ButterKnife.bind(this, view);

        if (bundle != null) {
            Workout workout = (Workout) Parcels.unwrap(bundle.getParcelable(EXTRA_WORKOUT));
            if (workout != null) {
                int i = 0;
                for (Exercise exercise : workout.getExercises()) {
                    act.displaySets(
                            200 + i,
                            exercise.getName(),
                            exercise.getSets(),
                            llExSubInsert,
                            llExSetsInsert
                    );
                }
            }
        }

       return view;
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
