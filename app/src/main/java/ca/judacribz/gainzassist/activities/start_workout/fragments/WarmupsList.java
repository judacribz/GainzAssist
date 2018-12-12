package ca.judacribz.gainzassist.activities.start_workout.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.models.Exercise;
import org.parceler.Parcels;

import static ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter.EXTRA_WARMUPS;

public class WarmupsList extends Fragment {

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
        act = (StartWorkout) context;
        bundle = this.getArguments();
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
        View view =  inflater.inflate(R.layout.fragment_warmups_list, container, false);
        ButterKnife.bind(this, view);

        if (bundle != null) {
            ArrayList<Exercise> warmups = Parcels.unwrap(bundle.getParcelable(EXTRA_WARMUPS));
            if (warmups != null) {

                for (int i = warmups.size()-1; i >= 0; --i) {
                    act.displaySets(
                            100 + i,
                            warmups.get(i),
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
