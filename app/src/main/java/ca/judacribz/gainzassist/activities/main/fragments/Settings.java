package ca.judacribz.gainzassist.activities.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.activities.main.Main;

import static ca.judacribz.gainzassist.util.Preferences.setTheme;

public class Settings extends Fragment {

    // Constants
    // --------------------------------------------------------------------------------------------

    private static final Settings INST = new Settings();
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
    public Settings() {
    }

    public static Settings getInstance() {
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
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);


        return view;
    }
    //Fragment//Override///////////////////////////////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================

    @OnClick(R.id.btnBlue)
    public void setBlue() {
        setTheme(this.getContext(), "blue");
        act.setTheme(R.style.BlueTheme);
        act.recreate();
    }
    @OnClick(R.id.btnGreen)
    public void setGreen() {

        setTheme(this.getContext(), "green");
        act.setTheme(R.style.GreenTheme);
        act.recreate();
    }
    //=Click=Handling==============================================================================
}
