package ca.judacribz.gainzassist.activity_workouts_list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;

public class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>{

    // Interfaces
    // --------------------------------------------------------------------------------------------
    private WorkoutClickObserver workoutClickObserver;

    public interface WorkoutClickObserver {
        void onWorkoutClick(String workoutName);
    }

    public void setVideoClickObserver(WorkoutClickObserver workoutClickObserver) {
        this.workoutClickObserver = workoutClickObserver;
    }
    // --------------------------------------------------------------------------------------------

    private int numItems;
    private ArrayList<String> btnNames;

    // Adapter Constructor
    public ButtonAdapter(ArrayList<String> btnNames) {
        this.numItems = btnNames.size();
        this.btnNames = btnNames;
    }

    // RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder> Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public ButtonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_button, parent, false);

        return new ButtonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ButtonViewHolder holder, int position) {
        holder.bind(btnNames.get(position));
    }

    @Override
    public int getItemCount() {
        return numItems;
    }
    //RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>//Override///////////////////////////////


    // Custom ViewHolder class for the recyclerView
    // ============================================================================================
    class ButtonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Button listItemButtonView;

        // ViewHolder Constructor
        ButtonViewHolder(View itemView) {
            super(itemView);

            listItemButtonView = (Button) itemView.findViewById(R.id.btnListItem);
            listItemButtonView.setOnClickListener(this);
        }

        // Sets the text for each button item
        void bind(String btnText) {
            listItemButtonView.setText(btnText);
        }

        @Override
        public void onClick(View v) {
            workoutClickObserver.onWorkoutClick(listItemButtonView.getText().toString());
        }
    }
    // ============================================================================================
}