package dev.bachtran.lavaradio.dto.rest

import com.github.topi314.lavasrc.spotify.SpotifyAudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

sealed class SearchResultItem {
    abstract val type : String
    abstract val title: String
    abstract val author: String
    abstract val artworkUrl: String
    abstract val uri: String

    data class SearchResultTrack(
        override val type: String = "track",
        override val title: String,
        override val author: String,
        override val artworkUrl: String,
        override val uri: String,
        val duration: Long,
        val isStream: Boolean
    ) : SearchResultItem()

    data class SearchResultPlaylist(
        override val type: String = "playlist",
        override val title: String,
        override val author: String,
        override val artworkUrl: String,
        override val uri: String,
        val numItems: Int,
        val playlistType: String,
    ) : SearchResultItem()

    companion object {
        fun from(item: Any, contextUri: String): SearchResultItem {
            return when (item) {
                is AudioTrack -> SearchResultTrack(
                    title = item.info.title,
                    author = item.info.author,
                    artworkUrl = item.info.artworkUrl,
                    uri = item.info.uri,
                    duration = item.info.length,
                    isStream = item.info.isStream
                )
                is SpotifyAudioPlaylist -> SearchResultPlaylist(
                    title = item.name,
                    author = item.author ?: "",
                    artworkUrl = item.artworkURL ?: "",
                    uri = item.url ?: "",
                    numItems = item.totalTracks ?: 0,
                    playlistType = item.type.toString().lowercase(),
                )
                is AudioPlaylist -> SearchResultPlaylist(
                    title = item.name,
                    author = "",
                    artworkUrl = "",
                    uri = contextUri,
                    numItems = item.tracks.size,
                    playlistType = "playlist",
                )
                else -> throw IllegalArgumentException()
            }
        }
    }
}
