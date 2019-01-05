package ca.judacribz.gainzassist.activities.how_to_videos;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.BindView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import static ca.judacribz.gainzassist.activities.start_workout.StartWorkout.EXTRA_HOW_TO_VID;
import static ca.judacribz.gainzassist.util.UI.setInitView;
import static ca.judacribz.gainzassist.util.UI.setToolbar;

public class HowToVideos extends AppCompatActivity implements
        MaterialSearchView.OnQueryTextListener,
        SearchVideosTask.YouTubeSearchObserver,
       ThumbnailAdapter.VideoClickObserver,
       YouTubePlayer.OnInitializedListener,
       YouTubePlayer.PlayerStateChangeListener {

    // Constants
    // --------------------------------------------------------------------------------------------
    public static final String EXTRA_VIDEO_ID =
            "ca.judacribz.gainzassist.act_how_to_videos.EXTRA_VIDEO_ID";
    public static final String SEARCH_SPACE_STR = "%20";

    String URL = "https://www.googleapis.com/youtube/v3/search?" +  // default youtube search url
                 "part=snippet&" +                                  // search resource
                 "fields=items(id/videoId,snippet/title)&" +        // needed fields
                 "maxResults=10&" +                                 // number of results to show
                 "q=";                                              // search text
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    RecyclerView rvVideoList;
    ThumbnailAdapter thumbnailAdapter;
    LinearLayoutManager rvLayoutManager;
    SearchVideosTask task;

    YouTubePlayer player;
    YouTubePlayerSupportFragment ytpFmt;
    FragmentManager fmtMgr;
    FragmentTransaction fmtTxn;
    String videoId, exerciseName;

    boolean isFullScreen = false;
    boolean backPressed = false;

    @BindView(R.id.msvHowToVids) MaterialSearchView searchView;
    //---------------------------------------------------------------------------------------------

    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseName = getIntent().getStringExtra(EXTRA_HOW_TO_VID);
        setInitView(this, R.layout.activity_how_to_videos, "How To " + exerciseName, true);

        fmtMgr = getSupportFragmentManager();

        rvVideoList = (RecyclerView) findViewById(R.id.rv_video_list);
        ytpFmt = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.fmt_youtube);

        handleFmt(false);

        executeSearch("how to " + exerciseName);

        rvLayoutManager = new LinearLayoutManager(this);
        rvVideoList.setLayoutManager(rvLayoutManager);
        rvVideoList.setHasFixedSize(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        searchView.setOnQueryTextListener(this);
        searchView.setVoiceSearch(true);
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
        if (!ytpFmt.isHidden()) {
            handleFmt(false);
        } else {
            super.onBackPressed();
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
                if (isFullScreen) {
                    backPressed = true;
                    player.setFullscreen(false);
                } else {
                    player.release();
                }
            }
            fmtTxn.hide(ytpFmt);
        }

        fmtTxn.commitNow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mainMenu) {
        getMenuInflater().inflate(R.menu.menu_how_to, mainMenu);

        searchView.setMenuItem(mainMenu.findItem(R.id.act_search));

        return super.onCreateOptionsMenu(mainMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.act_search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////


    // MaterialSearchView.OnQueryTextListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onQueryTextChange(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        executeSearch(query);

        return false;
    }
    //MaterialSearchView.OnQueryTextListener//Override/////////////////////////////////////////////


    private void executeSearch(String query) {
        if (task != null) {
            task.setYouTubeSearchObserver(null);
        }
        task = new SearchVideosTask();
        task.setYouTubeSearchObserver(this);
        task.execute(URL.concat(query.replaceAll("\\s+", SEARCH_SPACE_STR))
                .concat("&key=")
                .concat(getString(R.string.google_api_key)));
    }


    // Interface Callback: SearchVideosTask.YouTubeSearchObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void videoSearchDataReceived(ArrayList<String> videoIds, ArrayList<String> videoTitles) {
        if (videoIds.size() > 0) {
            thumbnailAdapter = new ThumbnailAdapter(videoIds, videoTitles);
            rvVideoList.setAdapter(thumbnailAdapter);
            thumbnailAdapter.setVideoClickObserver(this);

        } else {
            Snackbar.make(rvVideoList, "No video results", Snackbar.LENGTH_SHORT).show();
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
                                        final YouTubePlayer player,
                                        boolean b) {
        this.player = player;
        player.setPlayerStateChangeListener(this);
        player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
            @Override
            public void onFullscreen(boolean isfullScreen) {

                HowToVideos.this.isFullScreen = isfullScreen;
                if (!isfullScreen) {
                    if (backPressed) {
                        player.release();
                    }
                } else {
                    Toast.makeText(HowToVideos.this, "Fullscreen", Toast.LENGTH_SHORT).show();

                    ytpFmt.initialize(getString(R.string.google_api_key), HowToVideos.this);
                }
            }

        });
        player.setShowFullscreenButton(true);
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
