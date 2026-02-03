package dev.bachtran.lavaradio.lavaplayer.service

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
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

    fun searchTrack(query: String, source: String): List<AudioTrackInfo>? {
        return searchManager.search(query, source)
    }

    fun playTrack(identifier: String) {

        /* TODO: Ensure we only load URL instead of unprocessed query */

        playerManager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                playbackManager.addTrack(track)
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {
                playlist.tracks.forEach { playbackManager.addTrack(it) }
            }
            override fun noMatches() {
                println("No track found for: $identifier")
            }
            override fun loadFailed(e: FriendlyException) {
                println("Could not load track: ${e.message}")
            }
        })
    }

    fun pause() = playbackManager.togglePause(true)
    fun resume() = playbackManager.togglePause(false)
    fun skip() = playbackManager.playNextTrack()
    fun stop() = playbackManager.stop()

    fun getPlaybackState() = playbackManager.getPlaybackState()
    fun getQueue() = playbackManager.getQueue()

}
