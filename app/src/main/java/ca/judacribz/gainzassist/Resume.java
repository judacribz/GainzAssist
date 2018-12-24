package ca.judacribz.gainzassist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.ButterKnife;

public class Resume extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    Main act;

    // UI Elements
    Button btnWorkouts;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // WarmupsList Constructor/Instance                                                        //
    // ######################################################################################### //
    public Resume() {
        // Required empty public constructor
    }

    public static Resume getInstance() {
        return new Resume();
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
        View view =  inflater.inflate(R.layout.fragment_resume, container, false);
        ButterKnife.bind(this, view);

        return view;
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================

    //=Click=Handling==============================================================================
}
