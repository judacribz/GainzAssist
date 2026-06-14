package ca.gainzassist.domain.usecase.workout

import ca.gainzassist.core.constants.ExerciseConst.BARBELL
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.ExerciseSet
import kotlin.math.max
import kotlin.math.roundToInt

class CalculateNextExerciseWeightUseCase {

    operator fun invoke(
        exercise: Exercise,
        finishedSets: List<ExerciseSet>
    ): Float {
        val targetWeight = exercise.weight
        val targetReps = exercise.reps
        val expectedSets = exercise.sets
        val minWeight = exercise.minWeight
        val increment = if (exercise.equipment == BARBELL) 10f else 5f

        if (
            targetWeight <= 0f ||
            targetReps <= 0 ||
            expectedSets <= 0 ||
            increment <= 0f
        ) {
            return max(minWeight, targetWeight)
        }

        val completedSets = finishedSets.take(expectedSets)



        val totalEquivalentWeight = completedSets.sumOf { set ->
            val repRatio = (set.reps.toFloat() / targetReps.toFloat())
                .coerceIn(0f, 1.25f)

            (set.weight * repRatio).toDouble()
        }.toFloat()

        // Missing sets count as 0, so not finishing all sets lowers the average.
        val averageEquivalentWeight = totalEquivalentWeight / expectedSets.toFloat()

        val rawNextWeight = averageEquivalentWeight + increment

        return max(
            minWeight,
            roundToNearestIncrement(rawNextWeight, increment)
        )
    }

    private fun roundToNearestIncrement(
        weight: Float,
        increment: Float
    ): Float = (weight / increment).roundToInt() * increment
}