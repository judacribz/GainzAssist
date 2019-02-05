package ca.judacribz.gainzassist.activities.main.fragments;

import android.arch.lifecycle.LiveData;
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
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.CALLING_ACTIVITY.WORKOUTS_LIST;
import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.EXTRA_CALLING_ACTIVITY;
import static ca.judacribz.gainzassist.util.UI.getTextString;
import static ca.judacribz.gainzassist.util.firebase.Database.deleteWorkoutFirebase;

public class Workouts extends Fragment implements SingleItemAdapter.ItemClickObserver {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final Workouts INST = new Workouts();
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    Main act;
    View view;

    SingleItemAdapter workoutAdapter;
    LinearLayoutManager layoutManager;
    DialogPlus dialog;

    WorkoutViewModel workoutViewModel;
    Observer<List<Workout>> workObs;
    LiveData<List<Workout>> workLiv;

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
        return INST;
    }
    // ######################################################################################### //


    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = (Main) context;

        dialog = DialogPlus.newDialog(act)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                    }
                })
                .setContentHolder(new ViewHolder(R.layout.dialog_workout))
                .setContentBackgroundResource(R.drawable.edit_text_box_blue)
                .setExpanded(true)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .create();
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
                view = inflater.inflate(R.layout.fragment_workouts, container, false)
        );
        setRetainInstance(true);

        workoutViewModel = ViewModelProviders.of(act).get(WorkoutViewModel.class);
        workoutNames = new ArrayList<>();

        layoutManager = new LinearLayoutManager(act);
        workoutsList.setLayoutManager(layoutManager);
        workoutsList.setHasFixedSize(true);

        workObs = new Observer<List<Workout>>() {
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
//                        workoutAdapter.setItems(workoutNames);
//                        workoutAdapter.notifyDataSetChanged();

                        displayWorkoutList(workoutNames);
                    }
                }
            }
        };
        workLiv = workoutViewModel.getAllWorkouts();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        workLiv.observe(act, workObs);
    }

    @Override
    public void onStop() {
        super.onStop();
        workLiv.removeObserver(workObs);
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

    String workoutName;
    @Override
    public void onItemLongClick(View view) {
        workoutName = getTextString((TextView) view);

        ((TextView) dialog.findViewById(R.id.tv_workout_name)).setText(workoutName);

        dialog.findViewById(R.id.btn_edit_workout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editWorkout(workoutName);
            }
        });

        dialog.findViewById(R.id.btn_delete_workout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWorkout(workoutName);
            }
        });

        dialog.show();
    }

    private void editWorkout(String workoutName) {
        Intent newWorkoutSummaryIntent = new Intent(
                act,
                NewWorkoutSummary.class
        );
        newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, WORKOUTS_LIST);

        extraKey = NewWorkoutSummary.EXTRA_WORKOUT;
        intent = newWorkoutSummaryIntent;

        workoutViewModel.getWorkoutFromName(act, workoutName);

        dialog.dismiss();
    }

    private void deleteWorkout(String workoutName) {
        workoutViewModel.deleteWorkout(workoutName);
        deleteWorkoutFirebase(workoutName);
        dialog.dismiss();
    }
    //SingleItemAdapter.ItemClickObserver//Override////////////////////////////////////////////////


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
