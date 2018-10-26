package ca.judacribz.gainzassist.activities.start_workout;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;

import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.models.Set;


public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetsViewHolder> {

    private int numberOfSets;
    private ArrayList<Set> sets;

    // Adapter Constructor
    public SetsAdapter(ArrayList<Set> sets) {
        this.sets = sets;
        this.numberOfSets = sets.size();
    }

    // SetsAdapter @Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public SetsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.part_sets_input, parent, false);
        return new SetsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SetsViewHolder holder, int position) {

        Set set = sets.get(position);
        holder.bind(set.getSetNumber(), set.getReps(), set.getWeight());
    }

    @Override
    public int getItemCount() {
        return numberOfSets;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Returns the list of sets
    public ArrayList<Set> getArrayList() {
        return sets;
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
