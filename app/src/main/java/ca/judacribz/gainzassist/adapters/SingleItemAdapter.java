package ca.judacribz.gainzassist.adapters;

import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.FAIL;
import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.FAIL_SELECTED;
import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.SELECTED;
import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.SUCCESS;
import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.SUCCESS_SELECTED;
import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.UNSELECTED;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;

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
        FAIL,
        SUCCESS_SELECTED,
        FAIL_SELECTED
    }
    private SparseArray<PROGRESS_STATUS> progStatus;

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
    //RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder>//Override/////////////////////////////

    public void setItems(ArrayList<String> itemNames) {
        this.itemNames = itemNames;
    }

    public void setCurrItem(int currSetNum, boolean success) {
        setSelected(currSetNum);
        if (currSetNum > 1) {
            if (success) {
                progStatus.put(currSetNum - 2, SUCCESS);
            } else {
                progStatus.put(currSetNum - 2, FAIL);
            }
        }

        notifyDataSetChanged();
    }

    public void setSelected(int currSetNum) {
        deselectCurrSelected();
        currSetNum--;
        PROGRESS_STATUS status = progStatus.get(currSetNum);
        if (status != null) {
            switch (status) {
                case SUCCESS:
                    progStatus.put(currSetNum, SUCCESS_SELECTED);
                    break;

                case FAIL:
                    progStatus.put(currSetNum, FAIL_SELECTED);
                    break;

                default:
                    progStatus.put(currSetNum, SELECTED);
                    break;
            }
        }

        currSelected = currSetNum;

        notifyDataSetChanged();
    }

    private void deselectCurrSelected() {
        PROGRESS_STATUS status = progStatus.get(currSelected);
        if (status != null) {
            switch (status) {
                case SELECTED:
                    progStatus.put(currSelected, UNSELECTED);
                    break;

                case SUCCESS_SELECTED:
                    progStatus.put(currSelected, SUCCESS);
                    break;

                case FAIL_SELECTED:
                    progStatus.put(currSelected, FAIL);
                    break;
            }
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

            listItemView = itemView.findViewById(listItemId);
            listItemView.setOnClickListener(this);
            listItemView.setOnLongClickListener(this);
        }

        // Sets the text for each button item
        void bind(int pos) {
            listItemView.setText(itemNames.get(pos));

            if (progStatus != null) {
                int drawId = -1;
                if (pos < progStatus.size()) {
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

                        case SUCCESS_SELECTED:
                            drawId = R.drawable.textview_circle_success_selected;
                            break;

                        case FAIL_SELECTED:
                            drawId = R.drawable.textview_circle_fail_selected;
                            break;
                    }
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