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
import me.grantland.widget.AutofitHelper;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class SingleItemAdapter extends RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder> {


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

    private final Context context;
    private ArrayList<TextView> listViews = new ArrayList<>();
    private int listItemLayout, listItemId;
    private ArrayList<String> itemNames;
    private LayoutInflater inflater;
    private int currSelected = -1;
    private boolean dontRecycle = false;

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
        dontRecycle = true;
    }

    // RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder> Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(listItemLayout, parent, false);

        ItemViewHolder holder = new ItemViewHolder(view);
        if (dontRecycle) {
            holder.setIsRecyclable(false);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(itemNames.get(position), position);
    }

    @Override
    public int getItemCount() {
        return itemNames.size();
    }
    //RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder>//Override///////////////////////////////

    public void setItems(ArrayList<String> itemNames) {
        this.itemNames = itemNames;
    }

    public void setCurrItem(int currSetNum) {
        deselectAll(currSetNum);
        if (currSetNum <= listViews.size()) {
            listViews.get(currSetNum - 1).setBackground(context.getDrawable(R.drawable.textview_circle_selected));

            currSelected = -1;
        } else {
            currSelected = currSetNum - 1;
        }
    }

    private void deselectAll(int currSetInd) {
        for (TextView listView : listViews.subList(0, currSetInd - 1)) {
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
        void bind(String btnText, int position) {
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