package ca.gainzassist.core.constants

import android.util.SparseArray
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.FAIL
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.FAIL_SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SUCCESS
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SUCCESS_SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.UNSELECTED

object UIConst {
    @JvmField
    val PROGRESS_STATUS_MAP = object : SparseArray<ProgressStatus>() {
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
    val PROGRESS_CODE_MAP = object : HashMap<ProgressStatus, Int>() {
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
