package ca.judacribz.gainzassist.activities.add_workout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ca.judacribz.gainzassist.R;

import static ca.judacribz.gainzassist.util.UI.setInitView;

public class ExerciseEntry extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, R.layout.activity_exercise_entry, "", true);


    }
}
