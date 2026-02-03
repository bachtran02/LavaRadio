package dev.bachtran.lavaradio.common

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo

data class PlaybackState(
    val isPlaying: Boolean,
    val isPaused: Boolean,
    val position: Long,
    val track: AudioTrackInfo? = null
)
