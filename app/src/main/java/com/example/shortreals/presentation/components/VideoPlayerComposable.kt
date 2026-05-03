package com.example.shortreals.presentation.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

@Composable
fun VideoPlayerComposable(
    exoPlayer: ExoPlayer,
    thumbnailUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlayerReady by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (!isPlayerReady) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player != exoPlayer) {
                    playerView.player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        DisposableEffect(Unit) {
            val listener = object : androidx.media3.common.Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == androidx.media3.common.Player.STATE_READY) {
                        isPlayerReady = true
                    }
                }
            }
            exoPlayer.addListener(listener)
            onDispose {
                exoPlayer.removeListener(listener)
            }
        }
    }
}
