package ca.judacribz.gainzassist.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ca.judacribz.gainzassist.R;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class SingleItemAdapter extends RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder> {

    private final Context context;
    private ArrayList<TextView> listViews = new ArrayList<>();
    // Interfaces
    // --------------------------------------------------------------------------------------------
    private ItemClickObserver itemClickObserver;

    public interface ItemClickObserver {
        void onItemClick(View view);
        void onItemLongClick(View view);
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
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.itemNames = itemNames;
        this.listItemLayout = listItemLayout;
        this.listItemId = listItemId;
    }

    public SingleItemAdapter(Context context,
                             int numItems,
                             int listItemLayout,
                             int listItemId) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        this.itemNames = new ArrayList<>();
        for (int i = 1; i <= numItems; i++) {
            this.itemNames.add(String.valueOf(i));
        }

        this.listItemLayout = listItemLayout;
        this.listItemId = listItemId;
    }

    // RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder> Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(listItemLayout, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(itemNames.get(position));
    }

    @Override
    public int getItemCount() {
        return itemNames.size();
    }
    //RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder>//Override///////////////////////////////

    public void setItems(ArrayList<String> itemNames) {
        this.itemNames = itemNames;
    }

    int currSelected = -1;
    public void setCurrItem(int currSetNum) {
        deselectAll();
        if (currSetNum < listViews.size()) {
            listViews.get(currSetNum).setBackground(context.getDrawable(R.drawable.textview_circle_selected));
            currSetNum = -1;
        } else {
            currSelected = currSetNum;
        }
    }

    private void deselectAll() {
        for (TextView listView : listViews) {
            listView.setBackground(context.getDrawable(R.drawable.textview_circle));
        }
    }

    // Custom ViewHolder class for the recyclerView
    // ============================================================================================
    class ItemViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private TextView listItemView;

        // ViewHolder Constructor
        ItemViewHolder(View itemView) {
            super(itemView);

            listViews.add(listItemView = itemView.findViewById(listItemId));
            if (listViews.indexOf(listItemView) == currSelected) {
                listItemView.setBackground(context.getDrawable(R.drawable.textview_circle_selected));
            }

            listItemView.setOnClickListener(this);
            listItemView.setOnLongClickListener(this);

        }

        // Sets the text for each button item
        void bind(String btnText) {
            listItemView.setText(btnText);
        }

        @Override
        public void onClick(View view) {
            itemClickObserver.onItemClick(view);
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickObserver.onItemLongClick(view);
            return true;
        }
    }
    // ============================================================================================
}