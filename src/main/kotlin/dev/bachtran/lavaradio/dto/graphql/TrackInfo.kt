package dev.bachtran.lavaradio.dto.graphql

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo

/* GraphQL-compatible mirror for lavaplayer.AudioTrackInfo */
data class TrackInfo(
    val title: String,
    val author: String,
    val duration: Double,
    val identifier: String,
    val isStream: Boolean,
    val uri: String? = null,
    val artworkUrl: String? = null
) {
    companion object {
        fun from(info: AudioTrackInfo): TrackInfo {
            return TrackInfo(
                title = info.title,
                author = info.author,
                duration = info.length.toDouble(),
                identifier = info.identifier,
                isStream = info.isStream,
                uri = info.uri,
                artworkUrl = info.artworkUrl
            )
        }
    }
}