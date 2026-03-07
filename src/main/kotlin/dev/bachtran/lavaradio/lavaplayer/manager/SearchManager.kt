package dev.bachtran.lavaradio.lavaplayer.manager

import com.github.topi314.lavasearch.SearchManager
import com.github.topi314.lavasearch.result.AudioSearchResult
import com.github.topi314.lavasrc.spotify.SpotifyAudioPlaylist
import com.github.topi314.lavasrc.spotify.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.bachtran.lavaradio.dto.rest.SearchResultItem
import dev.bachtran.lavaradio.exception.InvalidSourceException
import dev.bachtran.lavaradio.exception.NoResultsFoundException
import dev.bachtran.lavaradio.lavaplayer.config.LavaplayerConfig
import jakarta.annotation.PostConstruct
import org.springframework.core.io.support.ResourcePatternUtils.isUrl
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

    fun searchQuery(query: String, source: String, types: String): List<SearchResultItem>? {

        if (isUrl(query)) {

            /* If query is URL then ignore source and types */
            return when (val searchResult = playerManager.loadItemSync(query)) {
                is AudioTrack, is SpotifyAudioPlaylist, is AudioPlaylist ->
                    listOf(SearchResultItem.from(searchResult, query))
                else -> throw NoResultsFoundException(query)
            }

        } else {
            val source = source.lowercase()
            val types = types.lowercase()

            when (source) {
                "spotify" -> {
                    val searchType = when (types) {
                        "album" -> AudioSearchResult.Type.ALBUM
                        "artist" -> AudioSearchResult.Type.ARTIST
                        "playlist" -> AudioSearchResult.Type.PLAYLIST
                        else -> AudioSearchResult.Type.TRACK
                    }
                    val searchResult = searchManager.loadSearch(
                        "spsearch:$query", mutableSetOf(searchType)
                    )

                    if (searchResult != null) {
                        when (searchType) {
                            AudioSearchResult.Type.ALBUM -> {
                                if (searchResult.albums != null) {
                                    return searchResult.albums
                                        .filter { it.name.isNotBlank() }
                                        .map { SearchResultItem.from(it, "") }
                                }
                            }
                            AudioSearchResult.Type.ARTIST -> {
                                if (searchResult.artists != null) {
                                    return searchResult.artists
                                        .filter { it.name.isNotBlank() }
                                        .map { SearchResultItem.from(it, "") }
                                }
                            }
                            AudioSearchResult.Type.PLAYLIST -> {
                                if (searchResult.playlists != null) {
                                    return searchResult.playlists
                                        .filter { it.name.isNotBlank() }
                                        .map { SearchResultItem.from(it, "") }
                                }
                            }
                            AudioSearchResult.Type.TRACK -> {
                                if (searchResult.tracks.isNotEmpty()) {
                                    return searchResult.tracks.map { SearchResultItem.from(it, "") }
                                }
                            }
                            else -> throw IllegalArgumentException()    /* should never reach here */
                        }
                    } else {
                        throw NoResultsFoundException(query)
                    }
                }
                "youtube", "soundcloud" -> {
                    val searchPrefix = when (source) {
                        "youtube" -> "ytsearch:"
                        "soundcloud" -> "scsearch:"
                        else -> throw IllegalArgumentException()    /* should never reach here */
                    }
                    return when (val searchResult = playerManager.loadItemSync("$searchPrefix:$query")) {
                        is AudioPlaylist -> searchResult.tracks.map { SearchResultItem.from(it, "") }
                        else -> throw NoResultsFoundException(query)
                    }
                }
                else -> throw InvalidSourceException(source)
            }
        }
        throw NoResultsFoundException(query)
    }

    fun cleanup() {
        searchManager.shutdown()
    }
}