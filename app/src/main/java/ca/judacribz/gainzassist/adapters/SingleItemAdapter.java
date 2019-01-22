package ca.judacribz.gainzassist.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.judacribz.gainzassist.R;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.*;

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
    private Context context;
    private LayoutInflater inflater;

    public enum PROGRESS_STATUS {
        UNSELECTED,
        SELECTED,
        SUCCESS,
        FAIL
    }
    private SparseArray<PROGRESS_STATUS> progStatus;

    private ArrayList<TextView> listViews;
    private ArrayList<String> itemNames;

    private int
            listItemLayout,
            listItemId,
            currSelected = -1;

    private boolean dontRecycle = false;

    // Adapter Constructor
    public SingleItemAdapter(Context context,
                             ArrayList<String> itemNames,
                             int listItemLayout,
                             int listItemId) {
        listViews = new ArrayList<>();

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.itemNames = itemNames;
        this.listItemLayout = listItemLayout;
        this.listItemId = listItemId;
    }

    public SingleItemAdapter(Context context,
                             int numItems,
                             int listItemLayout,
                             int listItemId, SparseArray<PROGRESS_STATUS> progStatus) {
        listViews = new ArrayList<>();

        this.context = context;
        this.inflater = LayoutInflater.from(context);

        this.itemNames = new ArrayList<>();
        for (int i = 1; i <= numItems; i++) {
            this.itemNames.add(String.valueOf(i));
        }
        this.progStatus = progStatus;
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
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return itemNames.size();
    }
    //RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder>//Override///////////////////////////////

    public void setItems(ArrayList<String> itemNames) {
        this.itemNames = itemNames;
    }

    public void setCurrItem(int currSetNum, boolean success) {

        progStatus.put(currSetNum - 1, SELECTED);
        if (currSetNum > 1) {
            if (success) {
                progStatus.put(currSetNum - 2, SUCCESS);
            } else {
                progStatus.put(currSetNum - 2, FAIL);
            }
        }

        notifyDataSetChanged();
//        deselectAll(currSetNum);
//        if (currSetNum <= listViews.size()) {
//            listViews.get(currSetNum - 1).setBackground(context.getDrawable(R.drawable.textview_circle_selected));
//
//            currSelected = -1;
//        } else {
//            currSelected = currSetNum - 1;
//        }
    }

    private void deselectAll(int currSetInd) {
//        for (TextView listView : listViews.subList(0, currSetInd - 1)) {
//            listView.setBackground(context.getDrawable(R.drawable.textview_circle));
//        }
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
            listItemView.setOnClickListener(this);
            listItemView.setOnLongClickListener(this);
        }

        // Sets the text for each button item
        void bind(int pos) {
            listItemView.setText(itemNames.get(pos));

            if (progStatus != null) {
                int drawId = -1;
                switch (progStatus.get(pos)) {
                    case UNSELECTED:
                        drawId = R.drawable.textview_circle;
                        break;

                    case SELECTED:
                        drawId = R.drawable.textview_circle_selected;
                        break;

                    case SUCCESS:
                        drawId = R.drawable.textview_circle_success;
                        break;

                    case FAIL:
                        drawId = R.drawable.textview_circle_fail;
                        break;
                }

                if (drawId != -1) {
                    listItemView.setBackground(context.getDrawable(drawId));
                }
            }
        }

        @Override
        public void onClick(View view) {
            if (itemClickObserver != null) {
                itemClickObserver.onItemClick(view);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickObserver != null) {
                itemClickObserver.onItemLongClick(view);
            }
            return true;
        }
    }

    // ============================================================================================
}