package dev.bachtran.lavaradio.dto

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo

data class PlaybackState(
    val isPlaying: Boolean,
    val isPaused: Boolean,
    val position: Long,
    val loop: String,
    val track: AudioTrackInfo? = null
)