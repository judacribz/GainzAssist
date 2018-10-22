package ca.judacribz.gainzassist.activities.workouts_list;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry;
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntrySummary;
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout;
import ca.judacribz.gainzassist.adapters.SingleItemAdapter;
import ca.judacribz.gainzassist.models.WorkoutHelper;

import static ca.judacribz.gainzassist.firebase.Database.deleteWorkoutFirebase;
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        workoutNames = workoutHelper.getAllWorkoutNames();
        displayWorkoutList(workoutNames);
    }

    /* Helper function to display button list of workouts */
    void displayWorkoutList(ArrayList<String> workouts) {
        filteredWorkouts = workouts;
        workoutAdapter = new SingleItemAdapter(
                this,
                workouts,
                R.layout.part_button,
                R.id.btnListItem
        );
        workoutAdapter.setItemClickObserver(this);
        workoutsList.setAdapter(workoutAdapter);

        // Add the text watcher to the search bar
        searchBar.addTextChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        workoutHelper.close();
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

    ///////////////////////////////////////////////////////////////////////////////////////////////


    // TextWatcher Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        filteredWorkouts = new ArrayList<>();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String filterWord = s.toString().toLowerCase();

        for (String workoutName : workoutNames) {
            if (workoutName.toLowerCase().contains(filterWord)) {
                filteredWorkouts.add(workoutName);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        workoutAdapter = new SingleItemAdapter(
                this,
                filteredWorkouts,
                R.layout.part_button,
                R.id.btnListItem
        );
        workoutAdapter.setItemClickObserver(this);
        workoutsList.setAdapter(workoutAdapter);
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

    // SingleItemAdapter.ItemLongClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onWorkoutLongClick(View anch, final String name) {
        View popupView = getLayoutInflater().inflate(R.layout.part_confirm_popup, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textView = popupView.findViewById(R.id.tv_workout_name);
        textView.setText(name);

        popupView.findViewById(R.id.btn_delete_workout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workoutHelper.deleteWorkout(name);
                deleteWorkoutFirebase(name);
                popupWindow.dismiss();
                workoutNames = workoutHelper.getAllWorkoutNames();
                displayWorkoutList(workoutNames);
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
