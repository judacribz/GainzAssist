package ca.gainzassist.models

import ca.gainzassist.core.constants.ExerciseConst
import ca.gainzassist.domain.model.Exercise
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseTest {

    @Test
    fun testEquipmentSetsMinWeightAndWeightChange_Barbell() {
        val exercise = Exercise()
        exercise.equipment = ExerciseConst.BARBELL
        assertEquals(45f, exercise.minWeight, 0.001f)
        assertEquals(5f, exercise.weightChange, 0.001f)
    }

    @Test
    fun testEquipmentSetsMinWeightAndWeightChange_Dumbbell() {
        val exercise = Exercise()
        exercise.equipment = ExerciseConst.DUMBBELL
        assertEquals(5f, exercise.minWeight, 0.001f)
        assertEquals(2.5f, exercise.weightChange, 0.001f)
    }

    @Test
    fun testEquipmentSetsMinWeightAndWeightChange_Other() {
        val exercise = Exercise()
        exercise.equipment = "NA"
        assertEquals(5f, exercise.minWeight, 0.001f)
        assertEquals(5f, exercise.weightChange, 0.001f)
    }
}
