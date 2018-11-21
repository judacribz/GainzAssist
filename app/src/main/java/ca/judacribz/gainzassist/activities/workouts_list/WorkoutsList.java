package ca.judacribz.gainzassist.activities.workouts_list;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary;
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener;
import ca.judacribz.gainzassist.models.*;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import org.parceler.Parcels;

import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.CALLING_ACTIVITY.WORKOUTS_LIST;
import static ca.judacribz.gainzassist.activities.add_workout.NewWorkoutSummary.EXTRA_CALLING_ACTIVITY;
import static ca.judacribz.gainzassist.util.firebase.Database.deleteWorkoutFirebase;
import static ca.judacribz.gainzassist.util.UI.setToolbar;

public class WorkoutsList extends AppCompatActivity
    implements SingleItemAdapter.ItemClickObserver,
               MaterialSearchView.OnQueryTextListener,
               OnWorkoutReceivedListener {

    public static final String EXTRA_WORKOUT
            = "ca.judacribz.gainzassist.activities.workouts_list.EXTRA_WORKOUT";

    // Global Variables
    // --------------------------------------------------------------------------------------------
    SingleItemAdapter workoutAdapter;
    LinearLayoutManager layoutManager;

    ArrayList<String> workoutNames;
    ArrayList<String> filteredWorkouts;
//    WorkoutHelper workoutHelper;

    // --------------------------------------------------------------------------------------------

    // ButterKnife Injections
    // --------------------------------------------------------------------------------------------
    @BindView(R.id.rv_workout_btns) RecyclerView workoutsList;
    @BindView(R.id.msvWorkouts) MaterialSearchView searchView;
    // --------------------------------------------------------------------------------------------

    WorkoutViewModel workoutViewModel;
    // WorkoutsList Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts_list);
        ButterKnife.bind(this);
        setToolbar(this, R.string.workouts, true);

        workoutNames = new ArrayList<>();

        // ExerciseSet the layout manager for the localWorkouts
        layoutManager = new LinearLayoutManager(this);
        workoutsList.setLayoutManager(layoutManager);
        workoutsList.setHasFixedSize(true);

        // Get all workouts from database
//        workoutHelper = new WorkoutHelper(this);
//        workoutHelper.close();

        workoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        workoutViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {
                if (workouts != null) {
                    Toast.makeText(WorkoutsList.this, workouts.size() + "", Toast.LENGTH_SHORT).show();
                    workoutNames = new ArrayList<>();
                    for (Workout workout : workouts) {
                        workoutNames.add(workout.getName());
                    }
                    displayWorkoutList(workoutNames);
                }
            }
        });

        searchView.setOnQueryTextListener(this);

        searchView.setVoiceSearch(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

//        workoutNames = workoutHelper.getAllWorkoutNames();
//        displayWorkoutList(workoutNames);
    }

    /* Helper function to display button list of workouts */
    public void displayWorkoutList(ArrayList<String> workouts) {
        filteredWorkouts = workouts;
        workoutAdapter = new SingleItemAdapter(
                this,
                workouts,
                R.layout.part_button,
                R.id.btnListItem
        );
        workoutAdapter.setItemClickObserver(this);
        workoutsList.setAdapter(workoutAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onStop() {
        super.onStop();
//        workoutHelper.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workouts_list, menu);

        MenuItem item = menu.findItem(R.id.act_search);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // TextWatcher Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onQueryTextChange(String newText) {

        filteredWorkouts = new ArrayList<>();
        String filterWord = newText.toLowerCase();

        for (String workoutName : workoutNames) {
            if (workoutName.toLowerCase().contains(filterWord)) {
                filteredWorkouts.add(workoutName);
            }
        }

        workoutAdapter = new SingleItemAdapter(
                this,
                filteredWorkouts,
                R.layout.part_button,
                R.id.btnListItem
        );
        workoutAdapter.setItemClickObserver(this);
        workoutsList.setAdapter(workoutAdapter);


        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    //TextWatcher//Override////////////////////////////////////////////////////////////////////////


    // SingleItemAdapter.ItemClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onWorkoutClick(String name) {
        extraKey = EXTRA_WORKOUT;
        intent = new Intent(this, StartWorkout.class);
        workoutViewModel.getWorkoutFromName(this, name);
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
                        getApplicationContext(),
                        NewWorkoutSummary.class
                );
                newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, WORKOUTS_LIST);

                extraKey = NewWorkoutSummary.EXTRA_WORKOUT;
                intent = newWorkoutSummaryIntent;

                workoutViewModel.getWorkoutFromName(WorkoutsList.this, workoutName);
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


    Intent intent;
    String extraKey;
    @Override
    public void onWorkoutsReceived(Workout workout) {
        intent.putExtra(extraKey, Parcels.wrap(workout));
        startActivity(intent);
    }

    // Click Handling
    // ============================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.act_add_workout:
                startActivity(new Intent(this, WorkoutEntry.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    // ============================================================================================
}
