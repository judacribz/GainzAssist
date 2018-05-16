package ca.judacribz.gainzassist.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.util.ArrayList;

public class SingleItemAdapter extends RecyclerView.Adapter<SingleItemAdapter.ButtonViewHolder> {

    // Interfaces
    // --------------------------------------------------------------------------------------------
    private ItemClickObserver itemClickObserver;

    public interface ItemClickObserver {
        void onWorkoutClick(String name);
    }

    public void setItemClickObserver(ItemClickObserver itemClickObserver) {
        this.itemClickObserver = itemClickObserver;
    }
    // --------------------------------------------------------------------------------------------

    private int numItems, listItemLayout, listItemId;
    private ArrayList<String> itemNames;
    private LayoutInflater inflater;

    // Adapter Constructor
    public SingleItemAdapter(Context context, ArrayList<String> itemNames, int listItemLayout, int listItemId) {
        this.inflater = LayoutInflater.from(context);
        this.numItems = itemNames.size();
        this.itemNames = itemNames;
        this.listItemLayout = listItemLayout;
        this.listItemId = listItemId;
    }

    // RecyclerView.Adapter<SingleItemAdapter.ButtonViewHolder> Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(listItemLayout, parent, false);

        return new ButtonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder holder, int position) {
        holder.bind(itemNames.get(position));
    }

    @Override
    public int getItemCount() {
        return numItems;
    }
    //RecyclerView.Adapter<SingleItemAdapter.ButtonViewHolder>//Override///////////////////////////////


    // Custom ViewHolder class for the recyclerView
    // ============================================================================================
    class ButtonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Button listItemButtonView;

        // ViewHolder Constructor
        ButtonViewHolder(View itemView) {
            super(itemView);

            listItemButtonView = (Button) itemView.findViewById(listItemId);
            listItemButtonView.setOnClickListener(this);
        }

        // Sets the text for each button item
        void bind(String btnText) {
            listItemButtonView.setText(btnText);
        }

        @Override
        public void onClick(View v) {
            itemClickObserver.onWorkoutClick(listItemButtonView.getText().toString());
        }
    }
    // ============================================================================================
}