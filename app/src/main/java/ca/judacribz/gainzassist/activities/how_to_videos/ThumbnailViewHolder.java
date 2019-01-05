package ca.judacribz.gainzassist.activities.how_to_videos;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import java.util.ArrayList;

import ca.judacribz.gainzassist.R;

public class ThumbnailViewHolder extends RecyclerView.ViewHolder implements
        YouTubeThumbnailView.OnInitializedListener,
        YouTubeThumbnailLoader.OnThumbnailLoadedListener {

    // Interfaces
    // --------------------------------------------------------------------------------------------
    private VideoClickObserver videoClickObserver;

    public interface VideoClickObserver {
        void onVideoClick(String videoId);
    }

    void setVideoClickObserver(VideoClickObserver videoClickObserver) {
        this.videoClickObserver = videoClickObserver;
    }
    // --------------------------------------------------------------------------------------------

    // Constants
    // --------------------------------------------------------------------------------------------
    private final static String THUMBNAIL_URL = "https://i.ytimg.com/vi/%s/default.jpg";
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------

    private YouTubeThumbnailLoader thumbnailLoader;
    private  String videoId;
    private ArrayList<String> videoIds;


    @BindView(R.id.yt_tv_video) YouTubeThumbnailView thumbnailView;
    @BindView(R.id.tv_video_title) TextView tvVideoTitle;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ThumbnailViewHolder Constructor                                                           //
    // ######################################################################################### //
    ThumbnailViewHolder(View itemView, ArrayList<String> videoIds) {
        super(itemView);

        ButterKnife.bind(this, itemView);
        this.videoIds = videoIds;
    }
    // ######################################################################################### //


    // Sets the thumbnail for each item
    void bind(String videoId, String videoTitle) {
        this.videoId = videoId;

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        tvVideoTitle.setText(videoTitle);
        thumbnailView.initialize(String.format(THUMBNAIL_URL, videoId), this);
    }


    // YouTubeThumbnailView.OnInitializedListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onInitializationSuccess(YouTubeThumbnailView thumbnailView,
                                        final YouTubeThumbnailLoader thumbnailLoader) {
        this.thumbnailView = thumbnailView;
        this.thumbnailLoader = thumbnailLoader;
        this.thumbnailLoader.setOnThumbnailLoadedListener(this);
        thumbnailLoader.setVideo(videoId);
    }

    @Override
    public void onInitializationFailure(YouTubeThumbnailView thumbnailView,
                                        YouTubeInitializationResult initializationResult) {
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // YouTubeThumbnailLoader.OnThumbnailLoadedListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onThumbnailLoaded(YouTubeThumbnailView thumbnailView, String s) {
        progressBar.setVisibility(View.GONE);
        thumbnailView.setVisibility(View.VISIBLE);
        thumbnailLoader.release();
    }

    @Override
    public void onThumbnailError(YouTubeThumbnailView thumbnailView,
                                 YouTubeThumbnailLoader.ErrorReason errorReason) {
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================
    @OnClick(R.id.btn_play_video)
    public void videoClick() {
        videoClickObserver.onVideoClick(videoIds.get(getLayoutPosition()));
    }
    // ============================================================================================
}