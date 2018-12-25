package ca.judacribz.gainzassist.activities.main.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary;
import ca.judacribz.gainzassist.activities.main.Main;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;

import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.CALLING_ACTIVITY.WORKOUTS_LIST;
import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.EXTRA_CALLING_ACTIVITY;
import static ca.judacribz.gainzassist.util.firebase.Database.deleteWorkoutFirebase;

public class Workouts extends Fragment implements SingleItemAdapter.ItemClickObserver {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
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
    @BindView(R.id.rv_workout_btns) RecyclerView workoutsList;
    // --------------------------------------------------------------------------------------------


    // ######################################################################################### //
    // WarmupsList Constructor/Instance                                                          //
    // ######################################################################################### //
    public Workouts() {
    }

    public static Workouts getInstance() {
        return new Workouts();
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
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_workouts, container, false);
        ButterKnife.bind(this, view);

        workoutNames = new ArrayList<>();

        // ExerciseSet the layout manager for the localWorkouts
        layoutManager = new LinearLayoutManager(act);
        workoutsList.setLayoutManager(layoutManager);
        workoutsList.setHasFixedSize(true);

        workoutViewModel = ViewModelProviders.of(act).get(WorkoutViewModel.class);
        workoutViewModel.getAllWorkouts().observe(act, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {
                if (workouts != null) {
                    workoutNames = new ArrayList<>();
                    for (Workout workout : workouts) {
                        workoutNames.add(workout.getName());
                    }
                    if (workoutAdapter == null) {
                        displayWorkoutList(workoutNames);
                    } else {
                        workoutAdapter.setItems(workoutNames);
                        workoutAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        return view;
    }

    /* Helper function to display button list of workouts */
    public void displayWorkoutList(@Nullable  ArrayList<String> workouts) {
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
    public void onWorkoutClick(String name) {
        intent = new Intent(act, StartWorkout.class);
        extraKey = Main.EXTRA_WORKOUT;
        workoutViewModel.getWorkoutFromName(act, name);
    }
    //SingleItemAdapter.ItemClickObserver//Override////////////////////////////////////////////////


    // SingleItemAdapter.ItemLongClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
//TODO change to dialogbar
    @Override
    public void onWorkoutLongClick(View anch, final String workoutName) {
        View popupView = getLayoutInflater().inflate(R.layout.part_confirm_popup, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textView = popupView.findViewById(R.id.tv_workout_name);
        textView.setText(workoutName);
        popupView.findViewById(R.id.btn_edit_workout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newWorkoutSummaryIntent = new Intent(
                        act,
                        NewWorkoutSummary.class
                );
                newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, WORKOUTS_LIST);

                extraKey = NewWorkoutSummary.EXTRA_WORKOUT;
                intent = newWorkoutSummaryIntent;

                workoutViewModel.getWorkoutFromName(act, workoutName);
            }
        });

        popupView.findViewById(R.id.btn_delete_workout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWorkoutFirebase(workoutName);
                popupWindow.dismiss();
            }
        });
        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);

        // If you need the PopupWindow to dismiss when when touched outside
//        popupWindow.setBackgroundDrawable(new ColorDrawable());
        // Get the View's(the one that was clicked in the Fragment) location
        int location[] = new int[2];
        anch.getLocationOnScreen(location);

        popupWindow.setHeight((anch.getHeight() - anch.getPaddingTop())*2 );
        popupWindow.setWidth(anch.getWidth() - anch.getPaddingStart());
        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anch, Gravity.NO_GRAVITY,
                location[0] + anch.getPaddingStart()/2, location[1]);
    }
    //SingleItemAdapter.ItemLongClickObserver//Override////////////////////////////////////////////////


    public void onQueryTextChange(String newText) {
        String filterWord = newText.toLowerCase();

        filteredWorkouts = new ArrayList<>();
        for (String workoutName : workoutNames) {
            if (workoutName.toLowerCase().contains(filterWord)) {
                filteredWorkouts.add(workoutName);
            }
        }

        workoutAdapter.setItems(filteredWorkouts);
        workoutAdapter.notifyDataSetChanged();
    }

    // Click Handling
    // ============================================================================================
    //=Click=Handling==============================================================================
}
