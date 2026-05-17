package ca.judacribz.gainzassist.activities.how_to_videos

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import ca.judacribz.gainzassist.R
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailView
import java.util.*

class ThumbnailViewHolder(itemView: View, private val videoIds: ArrayList<String>) : RecyclerView.ViewHolder(itemView),
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

    @BindView(R.id.yt_tv_video)
    lateinit var thumbnailView: YouTubeThumbnailView

    @BindView(R.id.tv_video_title)
    lateinit var tvVideoTitle: TextView

    @BindView(R.id.progress_bar)
    lateinit var progressBar: ProgressBar

    init {
        ButterKnife.bind(this, itemView)
    }

    fun bind(videoId: String, videoTitle: String) {
        this.videoId = videoId
        progressBar.progress = 0
        progressBar.visibility = View.VISIBLE
        tvVideoTitle.text = videoTitle
        thumbnailView.initialize(String.format(THUMBNAIL_URL, videoId), this)
    }

    override fun onInitializationSuccess(
        thumbnailView: YouTubeThumbnailView,
        thumbnailLoader: YouTubeThumbnailLoader
    ) {
        this.thumbnailView = thumbnailView
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
        progressBar.visibility = View.GONE
        thumbnailView.visibility = View.VISIBLE
        thumbnailLoader!!.release()
    }

    override fun onThumbnailError(
        thumbnailView: YouTubeThumbnailView,
        errorReason: YouTubeThumbnailLoader.ErrorReason
    ) {
    }

    @OnClick(R.id.btn_play_video)
    fun videoClick() {
        videoClickObserver?.onVideoClick(videoIds[layoutPosition])
    }

    companion object {
        private const val THUMBNAIL_URL = "https://i.ytimg.com/vi/%s/default.jpg"
    }
}
