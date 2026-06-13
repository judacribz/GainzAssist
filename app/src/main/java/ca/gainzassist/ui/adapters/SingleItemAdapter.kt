package ca.gainzassist.ui.adapters

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.gainzassist.R

class SingleItemAdapter : RecyclerView.Adapter<SingleItemAdapter.ItemViewHolder> {

    interface ItemClickObserver {
        fun onItemClick(view: View?)
        fun onItemLongClick(view: View?)
    }

    private var itemClickObserver: ItemClickObserver? = null

    fun setItemClickObserver(itemClickObserver: ItemClickObserver?) {
        this.itemClickObserver = itemClickObserver
    }

    private var context: Context? = null
    private var inflater: LayoutInflater? = null

    enum class ProgressStatus {
        UNSELECTED, SELECTED, SUCCESS, FAIL, SUCCESS_SELECTED, FAIL_SELECTED
    }

    private var progStatus: SparseArray<ProgressStatus>? = null
    private var itemNames: ArrayList<String>? = null
    private var listItemLayout = 0
    private var listItemId = 0
    private var currSelected = -1
    private var dontRecycle = false

    constructor(
        context: Context?,
        itemNames: ArrayList<String>?,
        listItemLayout: Int,
        listItemId: Int
    ) {
        this.context = context
        inflater = LayoutInflater.from(context)
        this.itemNames = itemNames
        this.listItemLayout = listItemLayout
        this.listItemId = listItemId
    }

    constructor(
        context: Context?,
        numItems: Int,
        listItemLayout: Int,
        listItemId: Int,
        progStatus: SparseArray<ProgressStatus>?
    ) {
        this.context = context
        inflater = LayoutInflater.from(context)
        itemNames = ArrayList()
        for (i in 1..numItems) {
            itemNames?.add(i.toString())
        }
        this.progStatus = progStatus
        this.listItemLayout = listItemLayout
        this.listItemId = listItemId
        dontRecycle = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(listItemLayout, parent, false)
        val holder = ItemViewHolder(view)
        if (dontRecycle) {
            holder.setIsRecyclable(false)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return itemNames?.size ?: 0
    }

    fun setItems(itemNames: ArrayList<String>?) {
        this.itemNames = itemNames
    }

    fun setCurrItem(currSetNum: Int, success: Boolean) {
        setSelected(currSetNum)
        if (currSetNum > 1) {
            val currentProgStatus = progStatus ?: return
            if (success) {
                currentProgStatus.put(currSetNum - 2, ProgressStatus.SUCCESS)
            } else {
                currentProgStatus.put(currSetNum - 2, ProgressStatus.FAIL)
            }
        }
        notifyDataSetChanged()
    }

    fun setSelected(currSetNum: Int) {
        var newCurrSetNum = currSetNum
        deselectCurrSelected()
        newCurrSetNum--
        val currentProgStatus = progStatus ?: return
        val status = currentProgStatus[newCurrSetNum]
        if (status != null) {
            when (status) {
                ProgressStatus.SUCCESS -> currentProgStatus.put(newCurrSetNum, ProgressStatus.SUCCESS_SELECTED)
                ProgressStatus.FAIL -> currentProgStatus.put(newCurrSetNum, ProgressStatus.FAIL_SELECTED)
                else -> currentProgStatus.put(newCurrSetNum, ProgressStatus.SELECTED)
            }
        }
        currSelected = newCurrSetNum
        notifyDataSetChanged()
    }

    private fun deselectCurrSelected() {
        val currentProgStatus = progStatus ?: return
        val status = currentProgStatus[currSelected]
        if (status != null) {
            when (status) {
                ProgressStatus.SELECTED -> currentProgStatus.put(currSelected, ProgressStatus.UNSELECTED)
                ProgressStatus.SUCCESS_SELECTED -> currentProgStatus.put(currSelected, ProgressStatus.SUCCESS)
                ProgressStatus.FAIL_SELECTED -> currentProgStatus.put(currSelected, ProgressStatus.FAIL)
                else -> {}
            }
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
        View.OnLongClickListener {
        private val listItemView: TextView = itemView.findViewById(listItemId)

        init {
            listItemView.setOnClickListener(this)
            listItemView.setOnLongClickListener(this)
        }

        fun bind(pos: Int) {
            listItemView.text = itemNames?.get(pos)
            if (progStatus != null) {
                var drawId = -1
                if (pos < progStatus!!.size()) {
                    when (progStatus!!.get(pos)) {
                        ProgressStatus.UNSELECTED -> drawId = R.drawable.textview_circle
                        ProgressStatus.SELECTED -> drawId = R.drawable.textview_circle_selected
                        ProgressStatus.SUCCESS -> drawId = R.drawable.textview_circle_success
                        ProgressStatus.FAIL -> drawId = R.drawable.textview_circle_fail
                        ProgressStatus.SUCCESS_SELECTED -> drawId = R.drawable.textview_circle_success_selected
                        ProgressStatus.FAIL_SELECTED -> drawId = R.drawable.textview_circle_fail_selected
                    }
                }
                if (drawId != -1) {
                    listItemView.background = context?.getDrawable(drawId)
                }
            }
        }

        override fun onClick(view: View) {
            if (itemClickObserver != null) {
                itemClickObserver?.onItemClick(view)
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (itemClickObserver != null) {
                itemClickObserver?.onItemLongClick(view)
            }
            return true
        }
    }
}
