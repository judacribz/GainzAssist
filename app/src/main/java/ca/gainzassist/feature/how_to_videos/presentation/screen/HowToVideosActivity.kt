package ca.gainzassist.feature.how_to_videos.presentation.screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.addCallback
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.gainzassist.R
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.feature.start_workout.presentation.screen.StartWorkoutActivity.Companion.EXTRA_HOW_TO_VID
import ca.gainzassist.feature.how_to_videos.presentation.viewmodel.HowToVideosViewModel
import ca.gainzassist.feature.how_to_videos.presentation.viewmodel.HowToVideosViewModelEvent
import ca.gainzassist.ui.components.HowToVideosTopBar
import ca.gainzassist.ui.components.HowToVideosTopBarActions
import ca.gainzassist.ui.components.HowToVideosTopBarState
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HowToVideosActivity : GainzBaseActivity() {

    private val viewModel: HowToVideosViewModel by viewModel()

    private var youTubePlayer: YouTubePlayer? = null
    private var pendingVideoId: String? = null
    private var exerciseName: String? = null

    // Create the YouTubePlayerView once to pass to AndroidView
    private val youTubePlayerView by lazy { YouTubePlayerView(this) }

    override fun onBeforeSetContent() {
        exerciseName = intent.getStringExtra(EXTRA_HOW_TO_VID)
        viewModel.initialize(exerciseName.orEmpty())

        onBackPressedDispatcher.addCallback(this) {
            navigateBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(youTubePlayerView)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is HowToVideosViewModelEvent.ShowMessage -> {
                            val v = window.decorView.rootView
                            Snackbar.make(v, event.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@HowToVideosActivity.youTubePlayer = youTubePlayer
                pendingVideoId?.let {
                    youTubePlayer.loadVideo(it, 0f)
                    pendingVideoId = null
                }
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                val currentVideoId = viewModel.state.value.selectedVideoId
                val v = window.decorView.rootView
                if (currentVideoId != null) {
                    Snackbar.make(v, "Unable to play this video", Snackbar.LENGTH_LONG)
                        .setAction("Open in YouTube") {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://www.youtube.com/watch?v=$currentVideoId".toUri()
                            )
                            startActivity(intent)
                        }
                        .show()
                } else {
                    Snackbar.make(v, "Unable to play this video", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    @Composable
    override fun InnerContent() {
        val state by viewModel.state.collectAsStateWithLifecycle()

        Column(Modifier.fillMaxSize()) {
            HowToVideosTopBar(
                state = HowToVideosTopBarState(
                    title = "How To ${state.exerciseName}",
                    isSearchExpanded = state.isSearchExpanded,
                    searchQuery = state.searchQuery
                ),
                actions = HowToVideosTopBarActions(
                    onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onSearchSubmit = {
                        val query = state.searchQuery.trim()
                        if (query.isNotEmpty()) {
                            viewModel.onSearchExpandedChanged(false)
                            viewModel.executeSearch(query)
                        } else {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Please enter a search term",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onSearchClick = {
                        viewModel.onSearchExpandedChanged(true)
                    },
                    onCloseSearchClick = {
                        viewModel.onSearchExpandedChanged(false)
                        viewModel.onSearchQueryChanged("")
                        viewModel.executeSearch(state.exerciseName)
                    },
                    onBackClick = { navigateBack() }
                )
            )

            HowToVideosScreen(
                uiState = HowToVideosUiState(
                    videos = state.videos,
                    isPlayerVisible = state.isPlayerVisible,
                    message = state.message
                ),
                onVideoClick = { onVideoClick(it) },
                playerContent = {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { youTubePlayerView }
                    )
                }
            )
        }
    }

    private fun navigateBack() {
        if (viewModel.state.value.isPlayerVisible) {
            closePlayer()
        } else {
            finish()
        }
    }

    private fun onVideoClick(videoId: String) {
        viewModel.onVideoClick(videoId)
        if (youTubePlayer != null) {
            youTubePlayer?.loadVideo(videoId, 0f)
        } else {
            pendingVideoId = videoId
        }
    }

    private fun closePlayer() {
        youTubePlayer?.pause()
        viewModel.closePlayer()
    }
}
