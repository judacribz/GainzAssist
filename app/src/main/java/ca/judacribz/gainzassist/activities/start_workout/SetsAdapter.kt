package ca.judacribz.gainzassist.activities.start_workout

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.models.ExerciseSet
import java.util.*

class SetsAdapter(private val exerciseSets: ArrayList<ExerciseSet>) :
    RecyclerView.Adapter<SetsAdapter.SetsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.part_sets_input, parent, false)
        return SetsViewHolder(parent.context, view)
    }

    override fun onBindViewHolder(holder: SetsViewHolder, position: Int) {
        val exerciseSet = exerciseSets[position]
        holder.bind(exerciseSet.setNumber, exerciseSet.reps, exerciseSet.weight)
    }

    override fun getItemCount(): Int {
        return exerciseSets.size
    }

    fun getArrayList(): ArrayList<ExerciseSet> {
        return exerciseSets
    }

    class SetsViewHolder(private val context: Context, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val tvSetNum: TextView = itemView.findViewById(R.id.tv_sets)
        private val etReps: EditText = itemView.findViewById(R.id.et_reps)
        private val etWeight: EditText = itemView.findViewById(R.id.et_weight)

        fun bind(setNumber: Int, reps: Int, weight: Float) {
            tvSetNum.text = (setNumber + 1).toString()
            etReps.setText(reps.toString())
            etWeight.setText(String.format(Locale.CANADA, "%.0f", weight))
        }
    }
}
