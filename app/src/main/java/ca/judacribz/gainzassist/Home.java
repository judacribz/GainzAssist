package ca.judacribz.gainzassist;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.judacribz.gainzassist.activities.authentication.Login;
import ca.judacribz.gainzassist.activities.workouts_list.WorkoutsList;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;

public class Home extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    Main act;

    // UI Elements
    @BindView(R.id.btn_workouts)
    Button btnWorkouts;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WarmupsList Constructor/Instance                                                        //
    // ######################################################################################### //
    public Home() {
        // Required empty public constructor
    }

    public static Home getInstance() {
        return new Home();
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
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        btnWorkouts.setText(R.string.workouts);

        return view;
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================

    @OnClick(R.id.btn_workouts)
    public void startWorkoutsList() {
        startActivity(new Intent(act, WorkoutsList.class));
    }
    //=Click=Handling==============================================================================
}
