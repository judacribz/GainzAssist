package ca.judacribz.gainzassist.activities.start_workout;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.models.ExerciseSet;


public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetsViewHolder> {

    private int numberOfSets;
    private ArrayList<ExerciseSet> exerciseSets;

    // Adapter Constructor
    SetsAdapter(ArrayList<ExerciseSet> exerciseSets) {
        this.exerciseSets = exerciseSets;
        this.numberOfSets = exerciseSets.size();
    }

    // SetsAdapter @Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public SetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.part_sets_input, parent, false);
        return new SetsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetsViewHolder holder, int position) {

        ExerciseSet exerciseSet = exerciseSets.get(position);
        holder.bind(exerciseSet.getSetNumber(), exerciseSet.getReps(), exerciseSet.getWeight());
    }

    @Override
    public int getItemCount() {
        return numberOfSets;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Returns the list of exerciseSets
    public ArrayList<ExerciseSet> getArrayList() {
        return exerciseSets;
    }


    // Custom ViewHolder class for the recyclerView
    // ============================================================================================
    class SetsViewHolder extends RecyclerView.ViewHolder {

        private TextView tvSetNum;
        private EditText etReps, etWeight;

        // ViewHolder Constructor
        SetsViewHolder(View itemView) {
            super(itemView);


            tvSetNum = itemView.findViewById(R.id.tv_sets);
            etReps = itemView.findViewById(R.id.et_num_reps);
            etWeight = itemView.findViewById(R.id.et_weight);
        }

        // Sets the text for each list item
        void bind(int setNumber, int reps, float weight) {
            tvSetNum.setText(String.valueOf(setNumber));
            etReps.setText(String.valueOf(reps));
            etWeight.setText(String.valueOf(weight));
        }
    }
}
