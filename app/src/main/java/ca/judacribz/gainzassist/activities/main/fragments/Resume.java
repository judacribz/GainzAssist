package ca.judacribz.gainzassist.activities.main.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.main.Main;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;

import java.util.ArrayList;
import java.util.Set;

import static ca.judacribz.gainzassist.util.Preferences.getIncompleteWorkouts;
import static ca.judacribz.gainzassist.util.UI.getTextString;

public class Resume extends Fragment implements SingleItemAdapter.ItemClickObserver {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final Resume INST = new Resume();
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    View view;
    Main act;
    SingleItemAdapter workoutAdapter;
    LinearLayoutManager layoutManager;

    WorkoutViewModel workoutViewModel;

    ArrayList<String>
            workoutNames,
            filteredWorkouts;

    public Intent intent;
    public String extraKey;

    // UI Elements
    @BindView(R.id.rv_res_workout_btns) RecyclerView workoutsList;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WarmupsList Constructor/Instance                                                        //
    // ######################################################################################### //
    public Resume() {
    }

    public static Resume getInstance() {
        return INST;
    }
    // ######################################################################################### //

    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (Main) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            return view;
        }

        ButterKnife.bind(
                this,
                view =  inflater.inflate(R.layout.fragment_resume, container, false)
        );
        setRetainInstance(true);

        // ExerciseSet the layout manager for the localWorkouts
        layoutManager = new LinearLayoutManager(act);
        workoutsList.setLayoutManager(layoutManager);
        workoutsList.setHasFixedSize(true);

        workoutViewModel = ViewModelProviders.of(act).get(WorkoutViewModel.class);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        filteredWorkouts = new ArrayList<>();
        Set<String> names = getIncompleteWorkouts(act);
        if (names != null) {
            filteredWorkouts.addAll(names);
            displayWorkoutList(null);
        }
    }

    /* Helper function to display button list of workouts */
    public void displayWorkoutList(@Nullable ArrayList<String> workouts) {
        if (workouts != null) {
            filteredWorkouts = workouts;
        }
        workoutAdapter = new SingleItemAdapter(
                act,
                filteredWorkouts,
                R.layout.part_button,
                R.id.btnListItem
        );
        workoutAdapter.setItemClickObserver(this);
        workoutsList.setAdapter(workoutAdapter);
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////



    // SingleItemAdapter.ItemClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onItemClick(View view) {
        intent = new Intent(act, StartWorkout.class);
        extraKey = Main.EXTRA_WORKOUT;
        workoutViewModel.getWorkoutFromName(act, getTextString((TextView) view));
    }

    @Override
    public void onItemLongClick(View view) {

    }
    //SingleItemAdapter.ItemClickObserver//Override////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================

    //=Click=Handling==============================================================================
}
