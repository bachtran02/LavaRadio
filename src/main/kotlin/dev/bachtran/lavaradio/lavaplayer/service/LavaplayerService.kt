package dev.bachtran.lavaradio.lavaplayer.service

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import dev.bachtran.lavaradio.exception.IdentifierIsNotUrlException
import dev.bachtran.lavaradio.exception.NoResultsFoundException
import dev.bachtran.lavaradio.lavaplayer.manager.PlaybackManager
import dev.bachtran.lavaradio.lavaplayer.manager.PlayerManager
import dev.bachtran.lavaradio.lavaplayer.manager.SearchManager
import org.springframework.stereotype.Service

@Service
class LavaplayerService(
    private val playerManager: PlayerManager,
    private val playbackManager: PlaybackManager,
    private val searchManager: SearchManager,
    private val audioProvider: AudioProvider,
) {
    fun provideFrame(): AudioFrame? = audioProvider.provide()

    fun searchTrack(query: String, source: String): List<AudioTrackInfo>? = searchManager.search(query, source)

    fun addTrack(identifier: String) {
        if (!isUrl(identifier)) {
            throw IdentifierIsNotUrlException("Identifier is not a valid URL: $identifier")
        }

        playerManager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                playbackManager.addTrack(track)
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {
                playbackManager.addTracks(playlist.tracks)
            }
            override fun noMatches() {
                throw NoResultsFoundException("No results found for identifier: $identifier")
            }
            override fun loadFailed(ex: FriendlyException) {
                throw ex
            }
        })
    }

    fun playTrack(identifier: String) {

        if (!isUrl(identifier)) {
            throw IdentifierIsNotUrlException("Identifier is not a valid URL: $identifier")
        }

        playerManager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                playbackManager.playTrack(track)
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {
                playbackManager.playTrack(playlist.tracks[0])
            }
            override fun noMatches() {
                throw NoResultsFoundException("No results found for identifier: $identifier")
            }
            override fun loadFailed(ex: FriendlyException) {
                throw ex
            }
        })
    }

    fun pause() = playbackManager.togglePause(true)

    fun resume() = playbackManager.togglePause(false)

    fun skip() = playbackManager.playNextTrack()

    fun stop() = playbackManager.stop()

    fun removeQueuedTrack(index: Int) {
        playbackManager.removeQueuedTrack(index)
    }

    fun moveQueuedTrack(trackUri: String, oldIndex: Int, newIndex: Int) {
        playbackManager.moveQueuedTrack(trackUri, oldIndex, newIndex)
    }

    fun getPlaybackState() = playbackManager.getPlaybackState()
    fun getQueue() = playbackManager.getQueue()

    private fun isUrl(input: String): Boolean {
        val urlRegex = Regex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*\$")
        return urlRegex.matches(input)
    }

}
