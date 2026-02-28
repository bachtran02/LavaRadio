package dev.bachtran.lavaradio.lavaplayer.service

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import dev.bachtran.lavaradio.dto.graphql.PlaybackState
import dev.bachtran.lavaradio.dto.graphql.PlaybackUpdateEvent
import dev.bachtran.lavaradio.dto.graphql.TrackInfo
import dev.bachtran.lavaradio.dto.rest.SearchResultItem
import dev.bachtran.lavaradio.exception.IdentifierIsNotUrlException
import dev.bachtran.lavaradio.exception.NoResultsFoundException
import dev.bachtran.lavaradio.lavaplayer.broadcaster.PlaybackBroadcaster
import dev.bachtran.lavaradio.lavaplayer.config.LavaplayerConfig
import dev.bachtran.lavaradio.lavaplayer.manager.PlaybackManager
import dev.bachtran.lavaradio.lavaplayer.manager.PlayerManager
import dev.bachtran.lavaradio.lavaplayer.manager.SearchManager
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class RadioService(
    lavaplayerConfig: LavaplayerConfig,
    private val playerManager: PlayerManager,
) {
    private val audioPlayer = playerManager.createPlayer()

    private val playbackManager = PlaybackManager(audioPlayer)

    private val playbackBroadcaster = PlaybackBroadcaster()

    private val searchManager = SearchManager(lavaplayerConfig, playerManager)

    @PostConstruct
    fun initHooks() {
        playbackManager.onTrackStartHook = {
            syncTrackChange(PlaybackUpdateEvent.TRACK_STARTED)
        }
        playbackManager.onTrackEndHook = {
            syncTrackChange(PlaybackUpdateEvent.TRACK_ENDED)
        }
        playbackManager.onTrackStuckHook = {
            syncTrackChange(PlaybackUpdateEvent.TRACK_STUCK)
        }
        playbackManager.onTrackException = {
            syncTrackChange(PlaybackUpdateEvent.TRACK_EXCEPTION)
        }
    }

    fun provideFrame(): AudioFrame? = audioPlayer.provide()

    // --- Playback Updates Stream ---

    fun getPlaybackUpdateStream() = playbackBroadcaster.stream()

    fun getPlaybackUpdateEvent(reason: String = "STATE_REQUESTED"): PlaybackUpdateEvent {
        return PlaybackUpdateEvent(
            eventType = reason,
            state = PlaybackState.from(playbackManager.getPlaybackState()),
            queue = playbackManager.getQueue().map{ TrackInfo.from(it) } ,
            history = playbackManager.getHistory().map { TrackInfo.from(it) }
        )
    }

    // --- Search & Data Retrieval ---

    fun searchQuery(query: String, source: String, types: String): List<SearchResultItem>? {
        return searchManager.searchQuery(query, source, types)
    }

    fun getPlaybackState() = playbackManager.getPlaybackState()

    fun getQueue() = playbackManager.getQueue()

    fun getHistory() = playbackManager.getHistory()

    // --- Track Loading ---

    fun addTrack(identifier: String, next: Boolean = false, shuffle: Boolean = false) {
        if (!isUrl(identifier)) {
            throw IdentifierIsNotUrlException("Identifier is not a valid URL: $identifier")
        }
        when (val searchResult = playerManager.loadItemSync(identifier)) {
            is AudioTrack -> {
                if (next) {
                    playbackManager.addTrack(searchResult, 0)
                } else {
                    playbackManager.addTrack(searchResult)
                }
            }
            is AudioPlaylist -> {
                val tracksToPlay = if (shuffle) searchResult.tracks.shuffled() else searchResult.tracks
                val insertionIndex = if (next) 0 else -1

                playbackManager.addTracks(tracksToPlay, insertionIndex)
            }
            else -> throw NoResultsFoundException("Failed to load track: $identifier")
        }
        syncQueueChange(PlaybackUpdateEvent.QUEUE_UPDATED)
    }

    fun playTrack(identifier: String) {

        if (!isUrl(identifier)) {
            throw IdentifierIsNotUrlException("Identifier is not a valid URL: $identifier")
        }
        when (val searchResult = playerManager.loadItemSync(identifier)) {
            is AudioTrack -> playbackManager.playTrack(searchResult)
            is AudioPlaylist -> playbackManager.playTrack(searchResult.tracks[0])
            else -> throw NoResultsFoundException("Failed to play track: $identifier")
        }
    }

    // --- Playback Controls ---

    fun pause() {
        playbackManager.togglePause(true)
        syncPlaybackStateChange(PlaybackUpdateEvent.PAUSE_TOGGLED)
    }

    fun resume() {
        playbackManager.togglePause(false)
        syncPlaybackStateChange(PlaybackUpdateEvent.PAUSE_TOGGLED)
    }

    fun skip() {
        playbackManager.playNextTrack()
        syncTrackChange(PlaybackUpdateEvent.TRACK_SKIPPED)
    }

    fun stop() {
        playbackManager.stop()
        syncTrackChange(PlaybackUpdateEvent.PLAYER_STOPPED)
    }

    fun seek(position: Long) {
        playbackManager.seek(position)
        syncPlaybackStateChange(PlaybackUpdateEvent.POSITION_SEEKED)
    }

    // --- Queue & Settings Management ---

    fun setLoop(mode: String) {
        playbackManager.setLoop(mode)
        syncPlaybackStateChange(PlaybackUpdateEvent.LOOP_MODE_CHANGED)
    }

    fun shuffleQueue() {
        playbackManager.shuffleQueue()
        syncQueueChange(PlaybackUpdateEvent.QUEUE_SHUFFLED)
    }

    fun removeQueuedTrack(index: Int) {
        playbackManager.removeQueuedTrack(index)
        syncQueueChange(PlaybackUpdateEvent.QUEUE_UPDATED)
    }

    fun moveQueuedTrack(trackUri: String, oldIndex: Int, newIndex: Int) {
        playbackManager.moveQueuedTrack(trackUri, oldIndex, newIndex)
        syncQueueChange(PlaybackUpdateEvent.QUEUE_UPDATED)
    }

    // --- Internal Helpers & Syncing ---

    private fun syncPlaybackStateChange(reason: String) {
        playbackBroadcaster.broadcastPlaybackStateChange(
            reason,
            PlaybackState.from(playbackManager.getPlaybackState())
        )
    }

    private fun syncTrackChange(reason: String) {
        playbackBroadcaster.broadcastTrackChange(
            reason,
            PlaybackState.from(playbackManager.getPlaybackState()),
            playbackManager.getQueue().map{ TrackInfo.from(it) } ,
            playbackManager.getHistory().map { TrackInfo.from(it) }
        )
    }

    private fun syncQueueChange(reason: String) {
        playbackBroadcaster.broadcastQueueChange(
            reason,
            playbackManager.getQueue().map{ TrackInfo.from(it) }
        )
    }

    private fun isUrl(input: String): Boolean {
        val urlRegex = Regex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*\$")
        return urlRegex.matches(input)
    }

}
