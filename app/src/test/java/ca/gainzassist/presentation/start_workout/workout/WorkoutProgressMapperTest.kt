package ca.gainzassist.presentation.start_workout.workout

import android.util.SparseArray
import ca.gainzassist.activities.start_workout.workout_screen.WorkoutProgressMapper
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.FAIL
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.FAIL_SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SUCCESS
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SUCCESS_SELECTED
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.UNSELECTED
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutProgressMapperTest {

    @Test
    fun setupProgress_createsCountItems_andSelectsCurrentOneBasedIndex() {
        val count = 3
        val selectedIndex = 2
        val progress = WorkoutProgressMapper.setupProgress(count, selectedIndex)
        
        assertEquals(UNSELECTED, progress.get(0))
        assertEquals(SELECTED, progress.get(1))
        assertEquals(UNSELECTED, progress.get(2))
    }

    @Test
    fun toProgressUiItems_returnsOneBasedNumbersAndCorrectStatuses() {
        val progress = SparseArray<ProgressStatus>()
        progress.put(0, SUCCESS)
        progress.put(1, SELECTED)
        
        val items = WorkoutProgressMapper.toProgressUiItems(progress, 2)
        assertEquals(2, items.size)
        assertEquals(1, items[0].number)
        assertEquals(SUCCESS, items[0].status)
        assertEquals(2, items[1].number)
        assertEquals(SELECTED, items[1].status)
    }

    @Test
    fun selectOneBased_convertsSuccessToSuccessSelected() {
        val progress = SparseArray<ProgressStatus>()
        progress.put(0, SUCCESS)
        
        WorkoutProgressMapper.selectOneBased(progress, 1)
        
        assertEquals(SUCCESS_SELECTED, progress.get(0))
    }

    @Test
    fun selectOneBased_convertsFailToFailSelected() {
        val progress = SparseArray<ProgressStatus>()
        progress.put(0, FAIL)
        
        WorkoutProgressMapper.selectOneBased(progress, 1)
        
        assertEquals(FAIL_SELECTED, progress.get(0))
    }

    @Test
    fun selectOneBased_selectingNewItemDeselectsOldSelectedToUnselected() {
        val progress = SparseArray<ProgressStatus>()
        progress.put(0, SELECTED)
        progress.put(1, UNSELECTED)
        
        WorkoutProgressMapper.selectOneBased(progress, 2)
        
        assertEquals(UNSELECTED, progress.get(0))
        assertEquals(SELECTED, progress.get(1))
    }

    @Test
    fun selectOneBased_deselectsOldSuccessSelectedToSuccess() {
        val progress = SparseArray<ProgressStatus>()
        progress.put(0, SUCCESS_SELECTED)
        progress.put(1, UNSELECTED)
        
        WorkoutProgressMapper.selectOneBased(progress, 2)
        
        assertEquals(SUCCESS, progress.get(0))
        assertEquals(SELECTED, progress.get(1))
    }

    @Test
    fun selectOneBased_deselectsOldFailSelectedToFail() {
        val progress = SparseArray<ProgressStatus>()
        progress.put(0, FAIL_SELECTED)
        progress.put(1, UNSELECTED)
        
        WorkoutProgressMapper.selectOneBased(progress, 2)
        
        assertEquals(FAIL, progress.get(0))
        assertEquals(SELECTED, progress.get(1))
    }

    @Test
    fun setCurrentOneBased_marksPreviousItemSuccessOrFail() {
        val progress = SparseArray<ProgressStatus>()
        progress.put(0, SELECTED)
        progress.put(1, UNSELECTED)
        
        // Move from 1 to 2, marking 1 as success
        WorkoutProgressMapper.setCurrentOneBased(progress, 2, true)
        
        assertEquals(SUCCESS, progress.get(0))
        assertEquals(SELECTED, progress.get(1))
        
        // Move from 2 to 3, marking 2 as fail
        progress.put(2, UNSELECTED)
        WorkoutProgressMapper.setCurrentOneBased(progress, 3, false)
        
        assertEquals(SUCCESS, progress.get(0))
        assertEquals(FAIL, progress.get(1))
        assertEquals(SELECTED, progress.get(2))
    }
}
