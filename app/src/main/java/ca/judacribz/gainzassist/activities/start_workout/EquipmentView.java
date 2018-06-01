package ca.judacribz.gainzassist.activities.start_workout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;


/**************************************************************************************************
 * start_workout -> EquipmentView.java                                                            *
 **************************************************************************************************
 *
 * Author(s)            | Content Added
 * ------------------------------------------------------------------------------------------------
 * Sheron Balasingam    | If the current exercise uses a barbell, this view draws a barbell with
 *                      | the least amount of weights that add up to the weight required in lbs.
 *-------------------------------------------------------------------------------------------------
 *                      |
 *
 * ================================================================================================
 * Used by StartWorkoutFragment
 *
 * This class is used to display a 2d image of the exercise equipment used.
 *
 * If the exercise uses a barbell, a barbell with the weights used will be displayed. This will
 * make it easier for the user to set up their barbell without needing to calculate how many plate
 * weights they may need (Uses a greedy algorithm to use the least amount of weights that
 * amount to the weight displayed).
 *
 **************************************************************************************************/
public class EquipmentView extends View {

    // Constants
    // ============================================================================================
    private final int TEXT_SIZE   = 30;
    private final int PLATE_WIDTH = 40;
    private final int[] WEIGHTS   = new int[] {45, 25, 10, 5};
    // ============================================================================================

    // Global Vars
    // ============================================================================================
    private int[] numWeights = new int[] {0, 0, 0, 0};

    int height = 0;
    float weight = -1f;
    String equipment = "";
    int diam45 = 45 * 45/10;
    int textSpacing = 0;

    String text;
    private Paint paint;
    ArrayList<RectF> plates;

    Context context;
    // ============================================================================================


    // Constructor
    public EquipmentView(Context context) {
        super(context);
        this.context = context;

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(TEXT_SIZE);
    }


    // ExercisesFragment @Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDraw(Canvas canvas) {
        this.height = canvas.getHeight();
        int width = canvas.getWidth();
        canvas.drawColor(Color.WHITE);

        // Draw the plate weights for the barbell
        if (plates != null) {
            for (RectF plate : plates) {
                canvas.drawRect(plate, paint);
            }

            // Text spacing for weight display
            textSpacing = this.height / 5;
            int i = textSpacing + TEXT_SIZE/2;

            // Draw text on the screen representing the number of each plate weight needed
            for (int j = 0; j < numWeights.length; j++) {

                if (numWeights[j] > 0) {
                    text = String.valueOf(numWeights[j] + "x" +  WEIGHTS[j] + "lbs");
                    canvas.drawText(text, width - text.length() * 20, i, paint);
                    i += textSpacing;
                }
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Sets up the RectF objects to be drawn in onDraw which represent the workout equipment
    public void setup(float weight, @Nullable String equipment) {
        if (equipment != null) {
            this.equipment = equipment;
        }

        // Setup barbell with weights
        if (this.equipment.equals(context.getString(R.string.barbell))) {
            this.weight = (weight - 45f) / 2;

            int startX = 10 , startY = 0;
            int newWeight = (int) this.weight;

            // Diameter for 45lb plate used to draw it
            diam45 = this.height - 40;
            plates = new ArrayList<>();

            /* Greedy algorithm to add the least amount of plate weights that adds up the the weight
             * required.
             */
            for (int j = 0; j < WEIGHTS.length; j++ ) {
                numWeights[j] = newWeight / WEIGHTS[j];

                // Sets the plate diameter using a fraction
                for (int i = 0; i < numWeights[j]; i++) {

                    // Sets weight based on fraction weight/45 multiplied by the length of 45
                    int r = WEIGHTS[j] * diam45 / 45 + (45 - WEIGHTS[j]);

                    // Sets the starting y position for drawing the plate weight
                    startY = 20 + (diam45 - r) / 2;

                    // Add plate rectangle side view to list of plates
                    plates.add(new RectF(startX, startY, startX + PLATE_WIDTH, startY + r));

                    // Increment the starting x position to draw
                    startX += PLATE_WIDTH + 5;
                }

                /* Subtracts the newly added weights multiplied by the quantity of it used from the
                 * weight capacity.
                 */
                newWeight -= WEIGHTS[j] * numWeights[j];
            }

            // Draws
            int center = this.height / 2 - PLATE_WIDTH / 2;
            plates.add(new RectF(0, center, startX + 30, center + PLATE_WIDTH));

        // TODO: dumbell, machine, equipment setups
        } else {
            this.weight = weight;
        }

        // Invalidate to redraw view
        invalidate();
    }
}