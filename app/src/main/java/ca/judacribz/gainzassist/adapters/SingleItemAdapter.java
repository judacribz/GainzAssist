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
        void onWorkoutLongClick(View anchor, String name);
    }

    public void setItemClickObserver(ItemClickObserver itemClickObserver) {
        this.itemClickObserver = itemClickObserver;
    }

    // --------------------------------------------------------------------------------------------

    private int listItemLayout, listItemId;
    private ArrayList<String> itemNames;
    private LayoutInflater inflater;

    // Adapter Constructor
    public SingleItemAdapter(Context context,
                             ArrayList<String> itemNames,
                             int listItemLayout,
                             int listItemId) {
        this.inflater = LayoutInflater.from(context);
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
        //
    }

    @Override
    public int getItemCount() {
        return itemNames.size();
    }
    //RecyclerView.Adapter<SingleItemAdapter.ButtonViewHolder>//Override///////////////////////////////

    public void setItems(ArrayList<String> itemNames) {
        this.itemNames = itemNames;
    }

    // Custom ViewHolder class for the recyclerView
    // ============================================================================================
    class ButtonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        private Button listItemButtonView;

        // ViewHolder Constructor
        ButtonViewHolder(View itemView) {
            super(itemView);

            listItemButtonView = (Button) itemView.findViewById(listItemId);
            listItemButtonView.setOnClickListener(this);
            listItemButtonView.setOnLongClickListener(this);
        }

        // Sets the text for each button item
        void bind(String btnText) {
            listItemButtonView.setText(btnText);
        }

        @Override
        public void onClick(View view) {
            itemClickObserver.onWorkoutClick(listItemButtonView.getText().toString());
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickObserver.onWorkoutLongClick(view, listItemButtonView.getText().toString());
            return true;
        }
    }
    // ============================================================================================
}