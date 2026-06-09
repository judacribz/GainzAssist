package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.constants.ExerciseConst.BARBELL
import ca.gainzassist.constants.ExerciseConst.DUMBBELL
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateNextExerciseWeightUseCaseTest {

    private lateinit var useCase: CalculateNextExerciseWeightUseCase

    @Before
    fun setup() {
        useCase = CalculateNextExerciseWeightUseCase()
    }

    private fun createExercise(
        equipment: String,
        weight: Float,
        reps: Int,
        sets: Int,
        minWeight: Float
    ): Exercise {
        return Exercise().apply {
            this.equipment = equipment
            this.weight = weight
            this.reps = reps
            this.sets = sets
            // The setter for equipment overrides minWeight, so we set it directly if needed or use the side-effect
            // To ensure minWeight is set for the test, we'll manually override it again
            this.minWeight = minWeight
        }
    }

    private fun createFinishedSets(count: Int, reps: Int, weight: Float): List<ExerciseSet> {
        return List(count) {
            ExerciseSet().apply {
                this.reps = reps
                this.weight = weight
            }
        }
    }

    @Test
    fun testBarbellExactTargetIncreases() {
        // Barbell target 100 x 10 x 3, actual all 100 x 10 → next 110.
        val exercise = createExercise(BARBELL, 100f, 10, 3, 45f)
        val finishedSets = createFinishedSets(3, 10, 100f)

        val nextWeight = useCase(exercise, finishedSets)
        assertEquals(110f, nextWeight, 0.1f)
    }

    @Test
    fun testBarbellHeavierActualIncreasesMoreThanOneStep() {
        // Barbell target 100 x 10 x 3, actual all 120 x 10 → next 130.
        val exercise = createExercise(BARBELL, 100f, 10, 3, 45f)
        val finishedSets = createFinishedSets(3, 10, 120f)

        val nextWeight = useCase(exercise, finishedSets)
        assertEquals(130f, nextWeight, 0.1f)
    }

    @Test
    fun testBarbellHigherRepsCappedAt1_25() {
        // Barbell target 100 x 10 x 3, actual all 100 x 15 → capped rep ratio 1.25, average 125, +10 = 135, rounded nearest 10 = 140.
        val exercise = createExercise(BARBELL, 100f, 10, 3, 45f)
        val finishedSets = createFinishedSets(3, 15, 100f)

        val nextWeight = useCase(exercise, finishedSets)
        assertEquals(140f, nextWeight, 0.1f)
    }

    @Test
    fun testBarbellLowerWeightButFullRepsKeepsSameTarget() {
        // Barbell target 100 x 10 x 3, actual all 90 x 10 → average 90, +10 = 100, next 100.
        val exercise = createExercise(BARBELL, 100f, 10, 3, 45f)
        val finishedSets = createFinishedSets(3, 10, 90f)

        val nextWeight = useCase(exercise, finishedSets)
        assertEquals(100f, nextWeight, 0.1f)
    }

    @Test
    fun testBarbellMuchLowerWeightRepsDropsMeaningfully() {
        // Barbell target 100 x 10 x 3, actual all 70 x 5 → average 35, +10 = 45, rounded nearest 10 = 50.
        val exercise = createExercise(BARBELL, 100f, 10, 3, 45f)
        val finishedSets = createFinishedSets(3, 5, 70f)

        val nextWeight = useCase(exercise, finishedSets)
        assertEquals(50f, nextWeight, 0.1f)
    }

    @Test
    fun testNonBarbellExactTargetIncreases() {
        // Non-barbell target 50 x 10 x 3, actual all 50 x 10 → next 55.
        val exercise = createExercise(DUMBBELL, 50f, 10, 3, 5f)
        val finishedSets = createFinishedSets(3, 10, 50f)

        val nextWeight = useCase(exercise, finishedSets)
        assertEquals(55f, nextWeight, 0.1f)
    }

    @Test
    fun testMissingSetsCountAsZero() {
        // Target 100 x 10 x 3. Actual 100 x 10 x 1 (2 sets missing).
        // Total equivalent = 100. Average equivalent = 100 / 3 = 33.33. +10 = 43.33. Rounded = 40.
        val exercise = createExercise(BARBELL, 100f, 10, 3, 20f)
        val finishedSets = createFinishedSets(1, 10, 100f)

        val nextWeight = useCase(exercise, finishedSets)
        assertEquals(40f, nextWeight, 0.1f)
    }

    @Test
    fun testNeverBelowMinWeight() {
        // Target 100 x 10 x 3. Actual 0 sets.
        // Result would normally drop to increment (10), but minWeight is 45. Should return 45.
        val exercise = createExercise(BARBELL, 100f, 10, 3, 45f)
        val finishedSets = emptyList<ExerciseSet>()

        val nextWeight = useCase(exercise, finishedSets)
        assertEquals(45f, nextWeight, 0.1f)
    }
}
