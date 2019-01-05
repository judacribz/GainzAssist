package ca.judacribz.gainzassist.activities.how_to_videos;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

import ca.judacribz.gainzassist.R;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailViewHolder> implements
        ThumbnailViewHolder.VideoClickObserver {

    // Interfaces
    // --------------------------------------------------------------------------------------------
    private VideoClickObserver videoClickObserver;

    public interface VideoClickObserver {
        void onVideoClick(String videoId);
    }

    void setVideoClickObserver(VideoClickObserver videoClickObserver) {
        this.videoClickObserver = videoClickObserver;
    }

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private ArrayList<String> videoIds;
    private ArrayList<String> videoTitles;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ThumbnailAdapter Constructor                                                              //
    // ######################################################################################### //
    ThumbnailAdapter(ArrayList<String> videoIds, ArrayList<String> videoTitles) {
        this.videoIds = videoIds;
        this.videoTitles = videoTitles;
    }
    // ######################################################################################### //


    // RecyclerView.Adapter<ThumbnailViewHolder> Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.part_thumbnail, parent, false);

        ThumbnailViewHolder thumbnailViewHolder = new ThumbnailViewHolder(view, videoIds);
        thumbnailViewHolder.setVideoClickObserver(this);

        return thumbnailViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        holder.bind(videoIds.get(position), videoTitles.get(position));
    }

    @Override
    public int getItemCount() {
        return videoIds.size();
    }
    //RecyclerView.Adapter<ThumbnailViewHolder>//Override//////////////////////////////////////////


    // Interface Callback: ThumbnailViewHolder.VideoClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onVideoClick(String videoId) {
        videoClickObserver.onVideoClick(videoId);
    }
    //ThumbnailViewHolder.VideoClickObserver//Override/////////////////////////////////////////////
}