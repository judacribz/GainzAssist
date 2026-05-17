package ca.judacribz.gainzassist.activities.start_workout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.constants.ExerciseConst.BARBELL
import ca.judacribz.gainzassist.constants.ExerciseConst.DUMBBELL
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
import java.util.*

class EquipmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val STROKE_WIDTH = 5f
    private val PLATE_WIDTH = 20f
    private val PLATE_HEIGHT = 100f
    private val BAR_WIDTH = 300f
    private val BAR_HEIGHT = 20f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var plates = ArrayList<Float>()
    private var weight = 0f
    private var equipment = BARBELL

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = STROKE_WIDTH
    }

    fun setWeight(weight: Float, equipment: String) {
        this.weight = weight
        this.equipment = equipment
        calculatePlates()
        invalidate()
    }

    private fun calculatePlates() {
        plates = ArrayList()
        var remainingWeight = weight
        val plateValues = floatArrayOf(45f, 35f, 25f, 10f, 5f, 2.5f)

        if (BARBELL == equipment) {
            remainingWeight -= BB_MIN_WEIGHT
        } else if (DUMBBELL == equipment) {
            remainingWeight -= DB_MIN_WEIGHT
        }

        remainingWeight /= 2f

        for (plateValue in plateValues) {
            while (remainingWeight >= plateValue) {
                plates.add(plateValue)
                remainingWeight -= plateValue
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        paint.color = ContextCompat.getColor(context, R.color.colorAccent)
        paint.style = Paint.Style.FILL

        // Draw bar
        canvas.drawRect(
            centerX - BAR_WIDTH / 2f,
            centerY - BAR_HEIGHT / 2f,
            centerX + BAR_WIDTH / 2f,
            centerY + BAR_HEIGHT / 2f,
            paint
        )

        // Draw plates
        var plateX: Float
        for (i in plates.indices) {
            val plateValue = plates[i]
            val heightRatio = plateValue / 45f
            val plateHeight = PLATE_HEIGHT * (0.5f + 0.5f * heightRatio)

            // Right side
            plateX = centerX + BAR_WIDTH / 2f - (i + 1) * PLATE_WIDTH
            canvas.drawRect(
                plateX,
                centerY - plateHeight / 2f,
                plateX + PLATE_WIDTH,
                centerY + plateHeight / 2f,
                paint
            )

            // Left side
            plateX = centerX - BAR_WIDTH / 2f + i * PLATE_WIDTH
            canvas.drawRect(
                plateX,
                centerY - plateHeight / 2f,
                plateX + PLATE_WIDTH,
                centerY + plateHeight / 2f,
                paint
            )
        }

        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 40f
        canvas.drawText("${weight}lb", centerX, centerY - PLATE_HEIGHT, paint)
    }
}
