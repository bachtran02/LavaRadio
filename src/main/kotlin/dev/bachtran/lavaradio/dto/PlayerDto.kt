package dev.bachtran.lavaradio.dto

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

data class PlaybackStateDTO(
    val isPlaying: Boolean,
    val isPaused: Boolean,
    val track: TrackInfoDTO? = null
)

data class TrackInfoDTO(
    val title: String,
    val author: String,
    val length: Long,
    val position: Long,
    val isStream: Boolean,
    val uri: String,
)
