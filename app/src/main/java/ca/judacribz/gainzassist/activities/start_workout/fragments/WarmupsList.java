package ca.judacribz.gainzassist.activities.start_workout.fragments;

import static ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter.EXTRA_WARMUPS;
import static ca.judacribz.gainzassist.models.Exercise.SetsType.WARMUP_SET;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkoutActivity;
import ca.judacribz.gainzassist.models.Exercise;

public class WarmupsList extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    StartWorkoutActivity act;
    Bundle bundle;

//    @BindView(R.id.ll_exercise_subtitle_insert) LinearLayout llExSubInsert;
    @BindView(R.id.ll_exercise_sets_insert) LinearLayout llExSetsInsert;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WarmupsList Constructor/Instance                                                        //
    // ######################################################################################### //
    public WarmupsList() {
        // Required empty public constructor
    }

    public static WarmupsList getInstance() {
        return new WarmupsList();
    }

    // ######################################################################################### //

    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (StartWorkoutActivity) context;
        bundle = this.getArguments();
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
        view =  inflater.inflate(R.layout.fragment_warmups_list, container, false);
        ButterKnife.bind(this, view);

        if (bundle != null) {
            ArrayList<Exercise> warmups = bundle.getParcelableArrayList(EXTRA_WARMUPS);
            if (warmups != null) {

                for (int i = warmups.size()-1; i >= 0; --i) {
                    act.displaySets(
                            WARMUP_SET,
                            warmups.get(i),
                            llExSetsInsert
                    );
                }
            }
        }

        return view;
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////
}
