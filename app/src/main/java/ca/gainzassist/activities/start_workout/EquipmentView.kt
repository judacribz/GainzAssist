package ca.gainzassist.activities.start_workout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import ca.gainzassist.R
import java.util.*

class EquipmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TEXT_SIZE = 50
    private val PLATE_WIDTH = 40
    private val WEIGHTS = intArrayOf(450, 250, 100, 50, 25)

    private val numWeights = intArrayOf(0, 0, 0, 0, 0)
    var viewHeight = 0
    var weight = -1f
    var equipment = ""
    var diam45 = 45 * 45 / 10
    var textSpacing = 0
    var text: String? = null
    private var paint: Paint = Paint()
    var plates: ArrayList<RectF>? = null

    init {
        paint = Paint()
    }

    override fun onDraw(canvas: Canvas) {
        this.viewHeight = height
        val width = width

        paint.color = Color.BLACK
        paint.strokeWidth = 0.5f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(0f, 0f, getWidth().toFloat(), getHeight().toFloat(), paint)

        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textSize = TEXT_SIZE.toFloat()

        if (plates != null) {
            for (plate in plates!!) {
                canvas.drawRect(plate, paint)
            }

            textSpacing = this.viewHeight / 5
            var i = textSpacing + TEXT_SIZE / 2

            for (j in numWeights.indices) {
                if (numWeights[j] > 0) {
                    text = String.format(Locale.getDefault(), "%dx%.1flbs", numWeights[j], WEIGHTS[j].toFloat() / 10f)
                    canvas.drawText(text!!, width.toFloat() - text!!.length * 20f, i.toFloat(), paint)
                    i += textSpacing
                }
            }
        }
    }

    fun setup(weight: Float, equipment: String?) {
        if (equipment != null) {
            this.equipment = equipment.lowercase(Locale.getDefault())
        }

        if (this.equipment == context.getString(R.string.barbell)) {
            this.weight = (weight - 45f) / 2
            this.weight *= 10
            var startX = 10
            var startY: Int
            var newWeight = this.weight.toInt()
            diam45 = this.viewHeight - 40
            plates = ArrayList()

            for (j in WEIGHTS.indices) {
                numWeights[j] = newWeight / WEIGHTS[j]
                for (i in 0 until numWeights[j]) {
                    if (j == 0 && i > 4) {
                        continue
                    }
                    val r = (WEIGHTS[j] / 10) * diam45 / 45 + (45 - (WEIGHTS[j] / 10))
                    startY = 20 + (diam45 - r) / 2
                    plates!!.add(RectF(startX.toFloat(), startY.toFloat(), (startX + PLATE_WIDTH).toFloat(), (startY + r).toFloat()))
                    startX += PLATE_WIDTH + 5
                }
                newWeight -= WEIGHTS[j] * numWeights[j]
            }
            val center = this.viewHeight / 2 - PLATE_WIDTH / 2
            plates!!.add(RectF(0f, center.toFloat(), (startX + 30).toFloat(), (center + PLATE_WIDTH).toFloat()))
        } else {
            this.weight = weight
        }
        invalidate()
    }
}
