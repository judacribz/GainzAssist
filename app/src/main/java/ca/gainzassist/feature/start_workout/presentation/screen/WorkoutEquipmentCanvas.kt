package ca.gainzassist.feature.start_workout.presentation.screen

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.gainzassist.R
import kotlin.math.min

private const val PLATE_WIDTH = 22f

@Composable
fun WorkoutEquipmentCanvas(
    weight: Float,
    equipment: String,
    modifier: Modifier = Modifier
) {
    val barbellName = stringResource(R.string.barbell).lowercase()
    val eqLower = equipment.lowercase()
    Canvas(modifier = modifier.padding(8.dp)) {
        val width = size.width
        val height = size.height

        // Prevent drawing crash during layout phase when size is too small
        if (width <= 0f || height <= 40f) return@Canvas

        if (eqLower == barbellName) {
            drawBarbell(weight, width, height)
        }
    }
}

fun DrawScope.drawBarbell(
    weight: Float,
    width: Float,
    height: Float
) {
    val barbellWeight = ((weight - 45f) / 2f * 10f).toInt()
    val diam45 = height - 20f

    val weights = intArrayOf(450, 250, 100, 50, 25)
    val numWeights = IntArray(5)

    val sleeveWidth = calculateSleeveWidth(barbellWeight, weights)
    val sleeveHeight = 16f

    drawSleeve(height, sleeveWidth, sleeveHeight)
    drawPlates(barbellWeight, weights, numWeights, diam45, height)
    drawBarbellText(numWeights, weights, width, height)
}

private fun calculateSleeveWidth(barbellWeight: Int, weights: IntArray): Float {
    var totalPlates = 0
    var tempWeight = barbellWeight
    for (j in weights.indices) {
        val qty = tempWeight / weights[j]
        val limit = if (j == 0) min(qty, 5) else qty
        totalPlates += limit
        tempWeight -= weights[j] * qty
    }
    return 20f + (totalPlates * PLATE_WIDTH) + 15f
}

private fun DrawScope.drawSleeve(height: Float, sleeveWidth: Float, sleeveHeight: Float) {
    val topLeft = Offset(0f, (height - sleeveHeight) / 2f)
    val size = Size(sleeveWidth, sleeveHeight)
    drawRect(color = Color.DarkGray, topLeft = topLeft, size = size, style = Fill)
    drawRect(color = Color.Black, topLeft = topLeft, size = size, style = Stroke(width = 2f))
}

private fun DrawScope.drawPlates(
    barbellWeight: Int,
    weights: IntArray,
    numWeights: IntArray,
    diam45: Float,
    height: Float
) {
    var startX = 20f
    var newWeight = barbellWeight
    for (j in weights.indices) {
        numWeights[j] = newWeight / weights[j]
        for (i in 0 until numWeights[j]) {
            if (j == 0 && i > 4) continue // Max 5 x 45 plates

            val ratio = weights[j] / 450f
            val r = diam45 * (0.4f + 0.6f * ratio)
            val startY = (height - r) / 2f

            drawRect(color = Color.Gray, topLeft = Offset(startX, startY), size = Size(PLATE_WIDTH, r))
            drawRect(
                color = Color.Black,
                topLeft = Offset(startX, startY),
                size = Size(PLATE_WIDTH, r),
                style = Stroke(width = 2f)
            )
            startX += PLATE_WIDTH
        }
        newWeight -= weights[j] * numWeights[j]
    }
}

private fun DrawScope.drawBarbellText(
    numWeights: IntArray,
    weights: IntArray,
    width: Float,
    height: Float
) {
    val distinctWeights = numWeights.count { it > 0 }
    if (distinctWeights == 0) return

    val textPaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 45f
        textAlign = Paint.Align.RIGHT
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val textSpacing = 60f
    var textY = (height - (distinctWeights * textSpacing)) / 2f + 45f

    for (j in numWeights.indices) {
        if (numWeights[j] > 0) {
            val lbs = weights[j] / 10f
            val label = "${numWeights[j]} x ${if (lbs % 1 == 0f) lbs.toInt().toString() else lbs.toString()} lbs"
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(label, width - 10f, textY, textPaint)
            }
            textY += textSpacing
        }
    }
}
