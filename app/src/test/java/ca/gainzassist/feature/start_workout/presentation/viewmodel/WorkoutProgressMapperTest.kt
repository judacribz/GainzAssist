package ca.gainzassist.feature.start_workout.presentation.viewmodel

import ca.gainzassist.ui.ProgressStatus
import ca.gainzassist.ui.ProgressStatus.FAIL
import ca.gainzassist.ui.ProgressStatus.FAIL_SELECTED
import ca.gainzassist.ui.ProgressStatus.SELECTED
import ca.gainzassist.ui.ProgressStatus.SUCCESS
import ca.gainzassist.ui.ProgressStatus.SUCCESS_SELECTED
import ca.gainzassist.ui.ProgressStatus.UNSELECTED
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutProgressMapperTest {

    @Test
    fun setupProgress_createsCountItems_andSelectsCurrentOneBasedIndex() {
        val count = 3
        val selectedIndex = 2
        val progress = WorkoutProgressMapper.setupProgress(count, selectedIndex)

        assertEquals(UNSELECTED, progress[0])
        assertEquals(SELECTED, progress[1])
        assertEquals(UNSELECTED, progress[2])
    }

    @Test
    fun toProgressUiItems_returnsOneBasedNumbersAndCorrectStatuses() {
        val progress = mutableMapOf<Int, ProgressStatus>()
        progress[0] = SUCCESS
        progress[1] = SELECTED

        val items = WorkoutProgressMapper.toProgressUiItems(progress, 2)
        assertEquals(2, items.size)
        assertEquals(1, items[0].number)
        assertEquals(SUCCESS, items[0].status)
        assertEquals(2, items[1].number)
        assertEquals(SELECTED, items[1].status)
    }

    @Test
    fun selectOneBased_convertsSuccessToSuccessSelected() {
        val progress = mutableMapOf<Int, ProgressStatus>()
        progress[0] = SUCCESS

        WorkoutProgressMapper.selectOneBased(progress, 1)

        assertEquals(SUCCESS_SELECTED, progress[0])
    }

    @Test
    fun selectOneBased_convertsFailToFailSelected() {
        val progress = mutableMapOf<Int, ProgressStatus>()
        progress[0] = FAIL

        WorkoutProgressMapper.selectOneBased(progress, 1)

        assertEquals(FAIL_SELECTED, progress[0])
    }

    @Test
    fun selectOneBased_selectingNewItemDeselectsOldSelectedToUnselected() {
        val progress = mutableMapOf<Int, ProgressStatus>()
        progress[0] = SELECTED
        progress[1] = UNSELECTED

        WorkoutProgressMapper.selectOneBased(progress, 2)

        assertEquals(UNSELECTED, progress[0])
        assertEquals(SELECTED, progress[1])
    }

    @Test
    fun selectOneBased_deselectsOldSuccessSelectedToSuccess() {
        val progress = mutableMapOf<Int, ProgressStatus>()
        progress[0] = SUCCESS_SELECTED
        progress[1] = UNSELECTED

        WorkoutProgressMapper.selectOneBased(progress, 2)

        assertEquals(SUCCESS, progress[0])
        assertEquals(SELECTED, progress[1])
    }

    @Test
    fun selectOneBased_deselectsOldFailSelectedToFail() {
        val progress = mutableMapOf<Int, ProgressStatus>()
        progress[0] = FAIL_SELECTED
        progress[1] = UNSELECTED

        WorkoutProgressMapper.selectOneBased(progress, 2)

        assertEquals(FAIL, progress[0])
        assertEquals(SELECTED, progress[1])
    }

    @Test
    fun setCurrentOneBased_marksPreviousItemSuccessOrFail() {
        val progress = mutableMapOf<Int, ProgressStatus>()
        progress[0] = SELECTED
        progress[1] = UNSELECTED

        // Move from 1 to 2, marking 1 as success
        WorkoutProgressMapper.setCurrentOneBased(progress, 2, true)

        assertEquals(SUCCESS, progress[0])
        assertEquals(SELECTED, progress[1])

        // Move from 2 to 3, marking 2 as fail
        progress[2] = UNSELECTED
        WorkoutProgressMapper.setCurrentOneBased(progress, 3, false)

        assertEquals(SUCCESS, progress[0])
        assertEquals(FAIL, progress[1])
        assertEquals(SELECTED, progress[2])
    }
}
