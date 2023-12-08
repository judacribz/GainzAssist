package ca.judacribz.gainzassist.activities.how_to_videos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.judacribz.gainzassist.R

// --------------------------------------------------------------------------------------------
// ######################################################################################### //
// ThumbnailAdapter Constructor                                                              //
// ######################################################################################### //
class ThumbnailAdapter internal constructor(
    private val videoIds: ArrayList<String>,
    private val videoTitles: ArrayList<String>
) : RecyclerView.Adapter<ThumbnailViewHolder>(), ThumbnailViewHolder.VideoClickObserver {
    // Interfaces
    // --------------------------------------------------------------------------------------------
    private var videoClickObserver: VideoClickObserver? = null

    interface VideoClickObserver {
        fun onVideoClick(videoId: String?)
    }

    fun setVideoClickObserver(videoClickObserver: VideoClickObserver?) {
        this.videoClickObserver = videoClickObserver
    }

    // ######################################################################################### //
    // RecyclerView.Adapter<ThumbnailViewHolder> Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.part_thumbnail, parent, false)
        val thumbnailViewHolder = ThumbnailViewHolder(view, videoIds)
        thumbnailViewHolder.setVideoClickObserver(this)
        return thumbnailViewHolder
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        holder.bind(videoIds[position], videoTitles[position])
    }

    override fun getItemCount(): Int {
        return videoIds.size
    }

    //RecyclerView.Adapter<ThumbnailViewHolder>//Override//////////////////////////////////////////
    // Interface Callback: ThumbnailViewHolder.VideoClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onVideoClick(videoId: String) {
        videoClickObserver!!.onVideoClick(videoId)
    } //ThumbnailViewHolder.VideoClickObserver//Override/////////////////////////////////////////////
}