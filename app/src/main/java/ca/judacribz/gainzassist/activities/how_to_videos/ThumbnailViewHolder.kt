package ca.judacribz.gainzassist.activities.how_to_videos

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import ca.judacribz.gainzassist.databinding.PartThumbnailBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.*

class ThumbnailViewHolder(private val binding: PartThumbnailBinding, private val videoIds: ArrayList<String>) : RecyclerView.ViewHolder(binding.root) {

    interface VideoClickObserver {
        fun onVideoClick(videoId: String)
    }

    private var videoClickObserver: VideoClickObserver? = null

    fun setVideoClickObserver(videoClickObserver: VideoClickObserver?) {
        this.videoClickObserver = videoClickObserver
    }

    init {
        binding.btnPlayVideo.setOnClickListener {
            videoClickObserver?.onVideoClick(videoIds[layoutPosition])
        }
    }

    fun bind(videoId: String, videoTitle: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvVideoTitle.text = videoTitle
        
        val url = String.format(THUMBNAIL_URL, videoId)
        
        Glide.with(binding.root.context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivThumbnail)
            
        binding.progressBar.visibility = View.GONE
    }

    companion object {
        private const val THUMBNAIL_URL = "https://i.ytimg.com/vi/%s/hqdefault.jpg"
    }
}
