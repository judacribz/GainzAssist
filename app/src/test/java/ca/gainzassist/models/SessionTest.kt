package ca.gainzassist.models

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTest {

    @Test
    fun testAddExerciseRecordsAvgWeights() {
        val session = Session()
        val exercise = Exercise()
        exercise.exerciseNumber = 0
        exercise.reps = 10
        exercise.weightChange = 5f
        
        val set1 = ExerciseSet().apply { reps = 10; weight = 100f }
        val set2 = ExerciseSet().apply { reps = 10; weight = 100f }
        exercise.finSets.add(set1)
        exercise.finSets.add(set2)

        session.addExercise(exercise)
        
        val recordedWeight = session.avgWeights.get(0, -1f)
        assertEquals(105f, recordedWeight, 0.01f)
    }
}
