package ca.judacribz.gainzassist.activity_how_to_videos;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import java.util.ArrayList;

import ca.judacribz.gainzassist.R;

public class ThumbnailViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener,
                   YouTubeThumbnailView.OnInitializedListener {

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
    private YouTubeThumbnailView thumbnailView;
    private TextView tvVideoTitle;
    private  String videoId;
    private ArrayList<String> videoIds;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ThumbnailViewHolder Constructor                                                           //
    // ######################################################################################### //
    ThumbnailViewHolder(View itemView, ArrayList<String> videoIds) {
        super(itemView);

        this.videoIds = videoIds;

        thumbnailView = (YouTubeThumbnailView) itemView.findViewById(R.id.yt_tv_video);
        tvVideoTitle = (TextView) itemView.findViewById(R.id.tv_video_title);

        ((ImageButton) itemView.findViewById(R.id.btn_play_video)).setOnClickListener(this);
    }
    // ######################################################################################### //


    // Sets the thumbnail for each item
    void bind(String videoId, String videoTitle) {
        this.videoId = videoId;

        tvVideoTitle.setText(videoTitle);
        thumbnailView.initialize(String.format(THUMBNAIL_URL, videoId), this);
    }


    // YouTubeThumbnailView.OnInitializedListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onInitializationSuccess(YouTubeThumbnailView thumbnailView,
                                        final YouTubeThumbnailLoader thumbnailLoader) {
        this.thumbnailView = thumbnailView;
        thumbnailLoader.setVideo(videoId);
    }

    @Override
    public void onInitializationFailure(YouTubeThumbnailView thumbnailView,
                                        YouTubeInitializationResult initializationResult) {
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Click Handling
    // ============================================================================================
    @Override
    public void onClick(View v) {
        videoClickObserver.onVideoClick(videoIds.get(getLayoutPosition()));
    }
    // ============================================================================================
}