package dev.bachtran.lavaradio.lavaplayer.manager

import com.github.topi314.lavasearch.SearchManager
import com.github.topi314.lavasearch.result.AudioSearchResult
import com.github.topi314.lavasrc.spotify.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import dev.bachtran.lavaradio.exception.InvalidSourceException
import dev.bachtran.lavaradio.exception.NoResultsFoundException
import dev.bachtran.lavaradio.lavaplayer.config.LavaplayerConfig
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class SearchManager(
    private val lavaplayerConfig: LavaplayerConfig,
    private val playerManager: PlayerManager,
) {
    val searchManager: SearchManager = SearchManager()

    @PostConstruct
    fun setup() {

        /* Set up Spotify as extra search source */
        val spotify = SpotifySourceManager(
            lavaplayerConfig.sources.spotify.clientId,
            lavaplayerConfig.sources.spotify.clientSecret,
            lavaplayerConfig.sources.spotify.spDc,
            lavaplayerConfig.sources.spotify.countryCode,
            null,
            null
        )
        searchManager.registerSearchManager(spotify)
    }

    fun search(query: String, source: String): List<AudioTrackInfo>? {

        when (source.lowercase()) {
            "spotify" -> {
                val searchResult = searchManager.loadSearch(
                    "spsearch:$query", mutableSetOf(AudioSearchResult.Type.TRACK)
                )
                if (searchResult != null && searchResult.tracks.isNotEmpty()) {
                    return searchResult.tracks.map { it.info }
                } else {
                    throw NoResultsFoundException("No results found on $source for query: $query" )
                }
            }
            "youtube", "soundcloud" -> {
                val searchPrefix = when (source.lowercase()) {
                    "youtube" -> "ytsearch:"
                    "soundcloud" -> "scsearch:"
                    else -> throw IllegalArgumentException()    /* should never reach here */
                }
                return when (val searchResult = playerManager.loadItemSync("$searchPrefix:$query")) {
                    is AudioPlaylist -> {
                        searchResult.tracks.map { it.info }
                    }
                    else -> {
                        throw NoResultsFoundException("No results found on $source for query: $query" )
                    }
                }
            }
            else -> {
                throw InvalidSourceException("Source '$source' is not supported for searching.")
            }
        }
    }
}