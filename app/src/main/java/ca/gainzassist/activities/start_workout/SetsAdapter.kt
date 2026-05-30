package ca.gainzassist.activities.start_workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.gainzassist.R
import ca.gainzassist.models.ExerciseSet
import java.util.Locale

class SetsAdapter(private val exerciseSets: ArrayList<ExerciseSet>) :
    RecyclerView.Adapter<SetsAdapter.SetsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.part_sets_input, parent, false)
        return SetsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetsViewHolder, position: Int) {
        val exerciseSet = exerciseSets[position]
        holder.bind(exerciseSet.setNumber, exerciseSet.reps, exerciseSet.weight)
    }

    override fun getItemCount(): Int {
        return exerciseSets.size
    }

    class SetsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val tvSetNum: TextView = itemView.findViewById(R.id.tv_sets)
        private val etReps: EditText = itemView.findViewById(R.id.et_reps)
        private val etWeight: EditText = itemView.findViewById(R.id.et_weight)

        fun bind(setNumber: Int, reps: Int, weight: Float) {
            tvSetNum.text = (setNumber.inc()).toString()
            etReps.setText(reps.toString())
            etWeight.setText(String.format(Locale.CANADA, "%.0f", weight))
        }
    }
}
