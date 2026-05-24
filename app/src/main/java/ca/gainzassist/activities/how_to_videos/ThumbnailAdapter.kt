package ca.gainzassist.activities.how_to_videos

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.gainzassist.databinding.PartThumbnailBinding
import java.util.*

class ThumbnailAdapter(
    private val videoIds: ArrayList<String>,
    private val videoTitles: ArrayList<String>
) : RecyclerView.Adapter<ThumbnailViewHolder>() {

    interface VideoClickObserver {
        fun onVideoClick(videoId: String)
    }

    private var videoClickObserver: VideoClickObserver? = null

    fun setVideoClickObserver(videoClickObserver: VideoClickObserver?) {
        this.videoClickObserver = videoClickObserver
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val binding = PartThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ThumbnailViewHolder(binding, videoIds)
        holder.setVideoClickObserver(object : ThumbnailViewHolder.VideoClickObserver {
            override fun onVideoClick(videoId: String) {
                videoClickObserver?.onVideoClick(videoId)
            }
        })
        return holder
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        holder.bind(videoIds[position], videoTitles[position])
    }

    override fun getItemCount(): Int {
        return videoIds.size
    }
}
