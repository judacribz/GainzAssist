package ca.judacribz.gainzassist.activities.workouts_list;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.add_workout.AddWorkout;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.models.WorkoutHelper;

import static ca.judacribz.gainzassist.util.UI.setToolbar;

public class WorkoutsList extends AppCompatActivity implements SingleItemAdapter.ItemClickObserver,
                                                               TextWatcher {

    public static final String EXTRA_WORKOUT_NAME
            = "ca.judacribz.gainzassist.activity_workouts_list.EXTRA_WORKOUT_NAME";

    // Global Variables
    // --------------------------------------------------------------------------------------------
    SingleItemAdapter workoutAdapter;
    LinearLayoutManager layoutManager;

    ArrayList<String> workoutNames;
    ArrayList<String> filteredWorkouts;
    WorkoutHelper workoutHelper;
    // --------------------------------------------------------------------------------------------

    // ButterKnife Injections
    // --------------------------------------------------------------------------------------------
    @BindView(R.id.rv_workout_btns) RecyclerView workoutsList;
    @BindView(R.id.et_workouts_search) EditText searchBar;
    // --------------------------------------------------------------------------------------------

    // WorkoutsList Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts_list);
        ButterKnife.bind(this);
        setToolbar(this, R.string.workouts, true);

        workoutNames = new ArrayList<>();

        // Set the layout manager for the localWorkouts
        layoutManager = new LinearLayoutManager(this);
        workoutsList.setLayoutManager(layoutManager);
        workoutsList.setHasFixedSize(true);

        // Get all workouts from database
        workoutHelper = new WorkoutHelper(this);
        workoutNames = workoutHelper.getAllWorkoutNames();
        displayWorkoutList(workoutNames);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workouts_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //TODO: update list/sqlite/firebase on AddWorkout return if successful
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* Helper function to display button list of workouts */
    void displayWorkoutList(ArrayList<String> workouts) {
        filteredWorkouts = workouts;
        workoutAdapter = new SingleItemAdapter(
                this,
                workouts,
                R.layout.list_item_button,
                R.id.btnListItem
        );
        workoutAdapter.setItemClickObserver(this);
        workoutsList.setAdapter(workoutAdapter);

        // Add the text watcher to the search bar
        searchBar.addTextChangedListener(this);
    }


    // TextWatcher Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        filteredWorkouts = new ArrayList<>();
        String filter = (searchBar.getText().toString().trim()).toLowerCase();

        for (String workoutName : workoutNames) {
            if (workoutName.toLowerCase().contains(filter)) {
                filteredWorkouts.add(workoutName);
            }
        }

        if (filteredWorkouts != null) {
            workoutAdapter = new SingleItemAdapter(this, filteredWorkouts, R.layout.list_item_button, R.id.btnListItem);
            workoutAdapter.setItemClickObserver(this);
            workoutsList.setAdapter(workoutAdapter);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
    //TextWatcher//Override////////////////////////////////////////////////////////////////////////


    // SingleItemAdapter.ItemClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onWorkoutClick(String name) {
        Intent startWorkoutIntent = new Intent(this, StartWorkout.class);
        startWorkoutIntent.putExtra(EXTRA_WORKOUT_NAME, name);
        startActivity(startWorkoutIntent);
    }
    //SingleItemAdapter.ItemClickObserver//Override////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        handleClick(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    /* Handles all clicks in activity */
    public void handleClick(int id) {
        switch (id) {
            case R.id.act_add_workout:
                startActivity(new Intent(this, AddWorkout.class));
            break;
        }
    }
    // ============================================================================================
}
