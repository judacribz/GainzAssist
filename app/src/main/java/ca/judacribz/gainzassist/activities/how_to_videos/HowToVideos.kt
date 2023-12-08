package ca.judacribz.gainzassist.activities.how_to_videos

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.how_to_videos.SearchVideosTask.YouTubeSearchObserver
import ca.judacribz.gainzassist.activities.start_workout.StartWorkoutActivity
import ca.judacribz.gainzassist.util.UI.setInitView
import com.google.android.material.snackbar.Snackbar
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.miguelcatalan.materialsearchview.MaterialSearchView

class HowToVideos : AppCompatActivity(), MaterialSearchView.OnQueryTextListener,
    YouTubeSearchObserver, ThumbnailAdapter.VideoClickObserver, YouTubePlayer.OnInitializedListener,
    PlayerStateChangeListener {
    var URL = "https://www.googleapis.com/youtube/v3/search?" +  // default youtube search url
            "part=snippet&" +  // search resource
            "fields=items(id/videoId,snippet/title)&" +  // needed fields
            "maxResults=10&" +  // number of results to show
            "q=" // search text

    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    var rvVideoList: RecyclerView? = null
    var thumbnailAdapter: ThumbnailAdapter? = null
    var rvLayoutManager: LinearLayoutManager? = null
    var task: SearchVideosTask? = null
    var player: YouTubePlayer? = null
    var ytpFmt: YouTubePlayerSupportFragment? = null
    var fmtMgr: FragmentManager? = null
    var fmtTxn: FragmentTransaction? = null
    var videoId: String? = null
    var exerciseName: String? = null
    var isFullScreen = false
    var backPressed = false

    @BindView(R.id.msvHowToVids)
    lateinit var searchView: MaterialSearchView

    //---------------------------------------------------------------------------------------------
    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseName = getIntent().getStringExtra(StartWorkoutActivity.EXTRA_HOW_TO_VID)
        setInitView(this, R.layout.activity_how_to_videos, "How To $exerciseName", true)
        fmtMgr = getSupportFragmentManager()
        rvVideoList = findViewById(R.id.rv_video_list) as RecyclerView?
        ytpFmt = getSupportFragmentManager().findFragmentById(R.id.fmt_youtube) as YouTubePlayerSupportFragment
        handleFmt(false)
        executeSearch("how to $exerciseName")
        rvLayoutManager = LinearLayoutManager(this)
        rvVideoList?.setLayoutManager(rvLayoutManager)
        rvVideoList?.setHasFixedSize(true)
    }

    override protected fun onResume() {
        super.onResume()
        searchView!!.setOnQueryTextListener(this)
        searchView!!.setVoiceSearch(true)
    }

    override fun onSupportNavigateUp(): Boolean {
//        if (ytpFmt.isHidden()) {
//            finish()
//        } else {
//            handleFmt(false)
//        }
        return true
    }

    override fun onBackPressed() {
//        if (!ytpFmt.isHidden()) {
//            handleFmt(false)
//        } else {
//            super.onBackPressed()
//        }
    }

    /* Handles showing or hiding the youtube player fragment */
    fun handleFmt(showFmt: Boolean) {
        fmtTxn = fmtMgr?.beginTransaction()
        if (showFmt) {
//            ytpFmt!!.initialize(getString(R.string.google_api_key), this)
//            fmtTxn.show(ytpFmt)
        } else {
            if (player != null) {
                if (isFullScreen) {
                    backPressed = true
                    player!!.setFullscreen(false)
                } else {
                    player!!.release()
                }
            }
//            fmtTxn?.hide(ytpFmt)
        }
        fmtTxn?.commitNow()
    }

    override fun onCreateOptionsMenu(mainMenu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_how_to, mainMenu)
        searchView!!.setMenuItem(mainMenu.findItem(R.id.act_search))
        return super.onCreateOptionsMenu(mainMenu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.act_search -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////
    // MaterialSearchView.OnQueryTextListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onQueryTextChange(query: String): Boolean {
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        executeSearch(query)
        return false
    }

    //MaterialSearchView.OnQueryTextListener//Override/////////////////////////////////////////////
    private fun executeSearch(query: String) {
        if (task != null) {
            task!!.setYouTubeSearchObserver(null)
        }
        task = SearchVideosTask()
        task!!.setYouTubeSearchObserver(this)
        task!!.execute(
            URL + query.replace(
                "\\s+".toRegex(),
                SEARCH_SPACE_STR
            ) + "&key=" + getString(R.string.google_api_key)
        )
    }

    // Interface Callback: SearchVideosTask.YouTubeSearchObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun videoSearchDataReceived(
        videoIds: ArrayList<String>,
        videoTitles: ArrayList<String>
    ) {
        if (videoIds.size > 0) {
            thumbnailAdapter = ThumbnailAdapter(videoIds, videoTitles)
            rvVideoList?.setAdapter(thumbnailAdapter)
            thumbnailAdapter!!.setVideoClickObserver(this)
        } else {
            Snackbar.make(rvVideoList!!, "No video results", Snackbar.LENGTH_SHORT).show()
        }
    }

    //SearchVideosTask.YouTubeSearchObserver//Override/////////////////////////////////////////////
    // Interface Callback: ThumbnailAdapter.VideoClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onVideoClick(videoId: String?) {
        this.videoId = videoId
        handleFmt(true)
    }

    //ThumbnailAdapter.VideoClickObserver//Override////////////////////////////////////////////////
    // Interface Callback: YouTubePlayer.OnInitializedListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider,
        player: YouTubePlayer,
        b: Boolean
    ) {
        this.player = player
        player.setPlayerStateChangeListener(this)
        player.setOnFullscreenListener { isfullScreen ->
            isFullScreen = isfullScreen
            if (!isfullScreen) {
                if (backPressed) {
                    player.release()
                }
            } else {
                Toast.makeText(this@HowToVideos, "Fullscreen", Toast.LENGTH_SHORT).show()
//                ytpFmt?.initialize(getString(R.string.google_api_key), this@HowToVideos)
            }
        }
        player.setShowFullscreenButton(true)
        player.loadVideo(videoId)
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        initializationResult: YouTubeInitializationResult
    ) {
    }

    //YouTubePlayer.OnInitializedListener//Override////////////////////////////////////////////////
    // YouTubePlayer.PlayerStateChangeListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onLoading() {}
    override fun onLoaded(s: String) {}
    override fun onAdStarted() {}
    override fun onVideoStarted() {}
    override fun onVideoEnded() {
        handleFmt(false)
    }

    override fun onError(errorReason: YouTubePlayer.ErrorReason) {} //YouTubePlayer.PlayerStateChangeListener//Override////////////////////////////////////////////

    companion object {
        // Constants
        // --------------------------------------------------------------------------------------------
        const val EXTRA_VIDEO_ID = "ca.judacribz.gainzassist.act_how_to_videos.EXTRA_VIDEO_ID"
        const val SEARCH_SPACE_STR = "%20"
    }
}
