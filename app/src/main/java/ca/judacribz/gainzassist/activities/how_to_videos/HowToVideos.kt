package ca.judacribz.gainzassist.activities.how_to_videos

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import butterknife.BindView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout.Companion.EXTRA_HOW_TO_VID
import ca.judacribz.gainzassist.util.UI.setInitView
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.miguelcatalan.materialsearchview.MaterialSearchView
import java.util.*

class HowToVideos : AppCompatActivity(),
    MaterialSearchView.OnQueryTextListener,
    SearchVideosTask.YouTubeSearchObserver,
    ThumbnailAdapter.VideoClickObserver,
    YouTubePlayer.OnInitializedListener,
    YouTubePlayer.PlayerStateChangeListener {

    companion object {
        const val EXTRA_VIDEO_ID = "ca.judacribz.gainzassist.act_how_to_videos.EXTRA_VIDEO_ID"
        const val SEARCH_SPACE_STR = "%20"
    }

    var URL = ("https://www.googleapis.com/youtube/v3/search?" + // default youtube search url
            "part=snippet&" + // search resource
            "fields=items(id/videoId,snippet/title)&" + // needed fields
            "maxResults=10&" + // number of results to show
            "q=") // search text

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseName = intent.getStringExtra(EXTRA_HOW_TO_VID)
        setInitView(this, R.layout.activity_how_to_videos, "How To $exerciseName", true)

        fmtMgr = supportFragmentManager

        rvVideoList = findViewById(R.id.rv_video_list)
        ytpFmt = supportFragmentManager.findFragmentById(R.id.fmt_youtube) as YouTubePlayerSupportFragment?

        handleFmt(false)

        executeSearch("how to $exerciseName")

        rvLayoutManager = LinearLayoutManager(this)
        rvVideoList!!.layoutManager = rvLayoutManager
        rvVideoList!!.setHasFixedSize(true)
    }

    override fun onResume() {
        super.onResume()
        searchView.setOnQueryTextListener(this)
        searchView.setVoiceSearch(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (ytpFmt!!.isHidden) {
            finish()
        } else {
            handleFmt(false)
        }
        return true
    }

    override fun onBackPressed() {
        if (!ytpFmt!!.isHidden) {
            handleFmt(false)
        } else {
            super.onBackPressed()
        }
    }

    fun handleFmt(showFmt: Boolean) {
        fmtTxn = fmtMgr!!.beginTransaction()

        if (showFmt) {
            ytpFmt!!.initialize(getString(R.string.google_api_key), this)
            fmtTxn!!.show(ytpFmt!!)
        } else {
            if (player != null) {
                if (isFullScreen) {
                    backPressed = true
                    player!!.setFullscreen(false)
                } else {
                    player!!.release()
                }
            }
            fmtTxn!!.hide(ytpFmt!!)
        }

        fmtTxn!!.commitNow()
    }

    override fun onCreateOptionsMenu(mainMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_how_to, mainMenu)
        searchView.setMenuItem(mainMenu.findItem(R.id.act_search))
        return super.onCreateOptionsMenu(mainMenu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.act_search -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextChange(query: String): Boolean {
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        executeSearch(query)
        return false
    }

    private fun executeSearch(query: String) {
        if (task != null) {
            task!!.setYouTubeSearchObserver(null)
        }
        task = SearchVideosTask()
        task!!.setYouTubeSearchObserver(this)
        task!!.execute(
            URL + query.replace("\\s+".toRegex(), SEARCH_SPACE_STR) +
                    "&key=" +
                    getString(R.string.google_api_key)
        )
    }

    override fun videoSearchDataReceived(videoIds: ArrayList<String>, videoTitles: ArrayList<String>) {
        if (videoIds.size > 0) {
            thumbnailAdapter = ThumbnailAdapter(videoIds, videoTitles)
            rvVideoList!!.adapter = thumbnailAdapter
            thumbnailAdapter!!.setVideoClickObserver(this)
        } else {
            Snackbar.make(rvVideoList!!, "No video results", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onVideoClick(videoId: String) {
        this.videoId = videoId
        handleFmt(true)
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider,
        player: YouTubePlayer,
        b: Boolean
    ) {
        this.player = player
        player.setPlayerStateChangeListener(this)
        player.setOnFullscreenListener { isfullScreen ->
            this@HowToVideos.isFullScreen = isfullScreen
            if (!isfullScreen) {
                if (backPressed) {
                    player.release()
                }
            } else {
                Toast.makeText(this@HowToVideos, "Fullscreen", Toast.LENGTH_SHORT).show()
                ytpFmt!!.initialize(getString(R.string.google_api_key), this@HowToVideos)
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

    override fun onLoading() {}
    override fun onLoaded(s: String) {}
    override fun onAdStarted() {}
    override fun onVideoStarted() {}
    override fun onVideoEnded() {
        handleFmt(false)
    }

    override fun onError(errorReason: YouTubePlayer.ErrorReason) {}
}
