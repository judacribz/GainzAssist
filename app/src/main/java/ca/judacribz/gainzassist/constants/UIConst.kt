package ca.judacribz.gainzassist.constants

import android.util.SparseArray
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.*
import java.util.*

object UIConst {
    @JvmField
    val PROGRESS_STATUS_MAP = object : SparseArray<PROGRESS_STATUS>() {
        init {
            put(0, UNSELECTED)
            put(1, SELECTED)
            put(2, SUCCESS)
            put(3, FAIL)
            put(4, SUCCESS_SELECTED)
            put(5, FAIL_SELECTED)
        }
    }

    @JvmField
    val PROGRESS_CODE_MAP = object : HashMap<PROGRESS_STATUS, Int>() {
        init {
            put(UNSELECTED, 0)
            put(SELECTED, 1)
            put(SUCCESS, 2)
            put(FAIL, 3)
            put(SUCCESS_SELECTED, 4)
            put(FAIL_SELECTED, 5)
        }
    }
}
