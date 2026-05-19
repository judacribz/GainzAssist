package ca.judacribz.gainzassist.activities.how_to_videos

import android.support.v7.widget.RecyclerView
import android.view.View
import ca.judacribz.gainzassist.databinding.PartThumbnailBinding
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailView
import java.util.*

class ThumbnailViewHolder(private val binding: PartThumbnailBinding, private val videoIds: ArrayList<String>) : RecyclerView.ViewHolder(binding.root),
    YouTubeThumbnailView.OnInitializedListener,
    YouTubeThumbnailLoader.OnThumbnailLoadedListener {

    interface VideoClickObserver {
        fun onVideoClick(videoId: String)
    }

    private var videoClickObserver: VideoClickObserver? = null

    fun setVideoClickObserver(videoClickObserver: VideoClickObserver?) {
        this.videoClickObserver = videoClickObserver
    }

    private var thumbnailLoader: YouTubeThumbnailLoader? = null
    private var videoId: String? = null

    init {
        binding.btnPlayVideo.setOnClickListener {
            videoClickObserver?.onVideoClick(videoIds[layoutPosition])
        }
    }

    fun bind(videoId: String, videoTitle: String) {
        this.videoId = videoId
        binding.progressBar.progress = 0
        binding.progressBar.visibility = View.VISIBLE
        binding.tvVideoTitle.text = videoTitle
        binding.ytTvVideo.initialize(String.format(THUMBNAIL_URL, videoId), this)
    }

    override fun onInitializationSuccess(
        thumbnailView: YouTubeThumbnailView,
        thumbnailLoader: YouTubeThumbnailLoader
    ) {
        this.thumbnailLoader = thumbnailLoader
        this.thumbnailLoader!!.setOnThumbnailLoadedListener(this)
        thumbnailLoader.setVideo(videoId)
    }

    override fun onInitializationFailure(
        thumbnailView: YouTubeThumbnailView,
        initializationResult: YouTubeInitializationResult
    ) {
    }

    override fun onThumbnailLoaded(thumbnailView: YouTubeThumbnailView, s: String) {
        binding.progressBar.visibility = View.GONE
        binding.ytTvVideo.visibility = View.VISIBLE
        thumbnailLoader!!.release()
    }

    override fun onThumbnailError(
        thumbnailView: YouTubeThumbnailView,
        errorReason: YouTubeThumbnailLoader.ErrorReason
    ) {
    }

    companion object {
        private const val THUMBNAIL_URL = "https://i.ytimg.com/vi/%s/default.jpg"
    }
}
