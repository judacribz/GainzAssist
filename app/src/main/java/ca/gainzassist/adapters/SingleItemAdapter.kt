package ca.gainzassist.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ca.gainzassist.R
import java.util.*

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

    enum class PROGRESS_STATUS {
        UNSELECTED, SELECTED, SUCCESS, FAIL, SUCCESS_SELECTED, FAIL_SELECTED
    }

    private var progStatus: SparseArray<PROGRESS_STATUS>? = null
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
        progStatus: SparseArray<PROGRESS_STATUS>?
    ) {
        this.context = context
        inflater = LayoutInflater.from(context)
        itemNames = ArrayList()
        for (i in 1..numItems) {
            itemNames!!.add(i.toString())
        }
        this.progStatus = progStatus
        this.listItemLayout = listItemLayout
        this.listItemId = listItemId
        dontRecycle = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = inflater!!.inflate(listItemLayout, parent, false)
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
        return itemNames!!.size
    }

    fun setItems(itemNames: ArrayList<String>?) {
        this.itemNames = itemNames
    }

    fun setCurrItem(currSetNum: Int, success: Boolean) {
        setSelected(currSetNum)
        if (currSetNum > 1) {
            if (success) {
                progStatus!!.put(currSetNum - 2, PROGRESS_STATUS.SUCCESS)
            } else {
                progStatus!!.put(currSetNum - 2, PROGRESS_STATUS.FAIL)
            }
        }
        notifyDataSetChanged()
    }

    fun setSelected(currSetNum: Int) {
        var newCurrSetNum = currSetNum
        deselectCurrSelected()
        newCurrSetNum--
        val status = progStatus!![newCurrSetNum]
        if (status != null) {
            when (status) {
                PROGRESS_STATUS.SUCCESS -> progStatus!!.put(newCurrSetNum, PROGRESS_STATUS.SUCCESS_SELECTED)
                PROGRESS_STATUS.FAIL -> progStatus!!.put(newCurrSetNum, PROGRESS_STATUS.FAIL_SELECTED)
                else -> progStatus!!.put(newCurrSetNum, PROGRESS_STATUS.SELECTED)
            }
        }
        currSelected = newCurrSetNum
        notifyDataSetChanged()
    }

    private fun deselectCurrSelected() {
        val status = progStatus!![currSelected]
        if (status != null) {
            when (status) {
                PROGRESS_STATUS.SELECTED -> progStatus!!.put(currSelected, PROGRESS_STATUS.UNSELECTED)
                PROGRESS_STATUS.SUCCESS_SELECTED -> progStatus!!.put(currSelected, PROGRESS_STATUS.SUCCESS)
                PROGRESS_STATUS.FAIL_SELECTED -> progStatus!!.put(currSelected, PROGRESS_STATUS.FAIL)
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
            listItemView.text = itemNames!![pos]
            if (progStatus != null) {
                var drawId = -1
                if (pos < progStatus!!.size()) {
                    when (progStatus!![pos]) {
                        PROGRESS_STATUS.UNSELECTED -> drawId = R.drawable.textview_circle
                        PROGRESS_STATUS.SELECTED -> drawId = R.drawable.textview_circle_selected
                        PROGRESS_STATUS.SUCCESS -> drawId = R.drawable.textview_circle_success
                        PROGRESS_STATUS.FAIL -> drawId = R.drawable.textview_circle_fail
                        PROGRESS_STATUS.SUCCESS_SELECTED -> drawId = R.drawable.textview_circle_success_selected
                        PROGRESS_STATUS.FAIL_SELECTED -> drawId = R.drawable.textview_circle_fail_selected
                    }
                }
                if (drawId != -1) {
                    listItemView.background = context!!.getDrawable(drawId)
                }
            }
        }

        override fun onClick(view: View) {
            if (itemClickObserver != null) {
                itemClickObserver!!.onItemClick(view)
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (itemClickObserver != null) {
                itemClickObserver!!.onItemLongClick(view)
            }
            return true
        }
    }
}
