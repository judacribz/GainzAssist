package ca.judacribz.gainzassist.activities.how_to_videos;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import java.util.ArrayList;

import ca.judacribz.gainzassist.R;

import static ca.judacribz.gainzassist.activities.start_workout.fragments.CurrWorkout.EXTRA_HOW_TO_VID;
import static ca.judacribz.gainzassist.util.UI.setToolbar;

public class HowToVideos extends AppCompatActivity
        implements SearchVideosTask.YouTubeSearchObserver,
                   ThumbnailAdapter.VideoClickObserver,
                   YouTubePlayer.OnInitializedListener,
                   YouTubePlayer.PlayerStateChangeListener {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_VIDEO_ID =
            "ca.judacribz.gainzassist.act_how_to_videos.EXTRA_VIDEO_ID";

    String URL = "https://www.googleapis.com/youtube/v3/search?" +  // default youtube search url
                 "part=snippet&" +                                  // search resource
                 "fields=items(id/videoId,snippet/title)&" +        // needed fields
                 "maxResults=30&" +                                 // number of results to show
                 "q=how%20to%20";                                  // search text
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    RecyclerView rvVideoList;
    ThumbnailAdapter thumbnailAdapter;
    LinearLayoutManager rvLayoutManager;
    SearchVideosTask task;

    YouTubePlayer player;
    YouTubePlayerFragment ytpFmt;
    FragmentManager fmtMgr;
    FragmentTransaction fmtTxn;
    String videoId, exerciseName;
    //---------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_videos);
        exerciseName = getIntent().getStringExtra(EXTRA_HOW_TO_VID);
        setToolbar(this, "How To " + exerciseName, true);

        fmtMgr = getFragmentManager();

        rvVideoList = (RecyclerView) findViewById(R.id.rv_video_list);
        ytpFmt = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.fmt_youtube);

        handleFmt(false);

        task = new SearchVideosTask();
        task.setYouTubeSearchObserver(this);
        task.execute(URL.concat(exerciseName)
                        .concat("&key=")
                        .concat(getString(R.string.google_api_key)));

        rvLayoutManager = new LinearLayoutManager(this);
        rvVideoList.setLayoutManager(rvLayoutManager);
        rvVideoList.setHasFixedSize(true);
    }

    @Override
    public boolean onSupportNavigateUp() {

        if (ytpFmt.isHidden()) {
            finish();
        } else {
            handleFmt(false);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (ytpFmt.isHidden()) {
            super.onBackPressed();
        } else {
            handleFmt(false);
        }
    }

    /* Handles showing or hiding the youtube player fragment */
    void handleFmt(boolean showFmt) {
        fmtTxn = fmtMgr.beginTransaction();

        if (showFmt) {
            ytpFmt.initialize(getString(R.string.google_api_key), this);
            fmtTxn.show(ytpFmt);

        } else {
            if (player != null) {
                player.release();
            }

            fmtTxn.hide(ytpFmt);
        }

        fmtTxn.commit();
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////


    // Interface Callback: SearchVideosTask.YouTubeSearchObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void videoSearchDataReceived(ArrayList<String> videoIds, ArrayList<String> videoTitles) {
        if (videoIds.size() > 0) {
            thumbnailAdapter = new ThumbnailAdapter(videoIds, videoTitles);
            rvVideoList.setAdapter(thumbnailAdapter);

            thumbnailAdapter.setVideoClickObserver(this);
        } else {
            Toast.makeText(this, "No video results", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    //SearchVideosTask.YouTubeSearchObserver//Override/////////////////////////////////////////////


    // Interface Callback: ThumbnailAdapter.VideoClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onVideoClick(final String videoId) {
        this.videoId = videoId;
        handleFmt(true);
    }
    //ThumbnailAdapter.VideoClickObserver//Override////////////////////////////////////////////////


    // Interface Callback: YouTubePlayer.OnInitializedListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player,
                                        boolean b) {
        this.player = player;
        player.setPlayerStateChangeListener(this);
        player.setShowFullscreenButton(false);
        player.loadVideo(videoId);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult initializationResult) {
    }
    //YouTubePlayer.OnInitializedListener//Override////////////////////////////////////////////////


    // YouTubePlayer.PlayerStateChangeListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onLoading() {
    }

    @Override
    public void onLoaded(String s) {
    }

    @Override
    public void onAdStarted() {
    }

    @Override
    public void onVideoStarted() {
    }

    @Override
    public void onVideoEnded() {
        handleFmt(false);
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
    }
    //YouTubePlayer.PlayerStateChangeListener//Override////////////////////////////////////////////
}
