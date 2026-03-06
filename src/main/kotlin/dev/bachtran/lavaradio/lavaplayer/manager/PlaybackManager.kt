package dev.bachtran.lavaradio.lavaplayer.manager

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import dev.bachtran.lavaradio.dto.rest.PlaybackState
import dev.bachtran.lavaradio.exception.InvalidSeekException
import kotlin.text.lowercase

enum class LoopMode { QUEUE, TRACK, NONE }

class PlaybackManager(private val player: AudioPlayer) : AudioEventAdapter() {

    private val queueManager = QueueManager()

    private val playLock = Any()

    private var loopMode = LoopMode.NONE

    var onTrackStartHook: (() -> Unit)? = null

    var onTrackEndHook: (() -> Unit)? = null

    var onTrackStuckHook: (() -> Unit)? = null

    var onTrackException: (() -> Unit)? = null

    fun addListener() { player.addListener(this) }

    // --- Core Playback Commands ---

    fun playTrack(track: AudioTrack) = player.startTrack(track, false)

    fun playNextTrack() = player.startTrack(queueManager.popNextTrack(), false)

    fun togglePause(shouldPause: Boolean) {
        player.isPaused = shouldPause
    }

    fun stop() {
        synchronized(playLock) {
            queueManager.clearQueue()
            player.stopTrack()
            queueManager.clearHistory()
        }
    }

    fun seek(position: Long) {
        if (player.playingTrack.info.isStream || position < 0 || position > player.playingTrack.duration) {
            throw InvalidSeekException(position)
        }
        player.playingTrack?.position = position
    }

    // --- Track Loading Logic ---

    fun addTrack(track: AudioTrack, index: Int = -1) {
        synchronized(playLock) {
            if (player.playingTrack == null) {
                playTrack(track)
            } else {
                queueManager.addTrack(track, index)
            }
        }
    }

    fun addTracks(tracks: List<AudioTrack>, index: Int = -1) {
        if (tracks.isEmpty()) return

        synchronized(playLock) {
            if (player.playingTrack == null) {
                playTrack(tracks[0])
                if (tracks.size > 1) {
                    queueManager.addTracks(tracks.drop(1))
                }
            } else {
                /* Player is playing, so there may be tracks before this */
                queueManager.addTracks(tracks, index)
            }
        }
    }

    // --- State & Settings ---

    fun setLoop(mode: LoopMode) { loopMode = mode }

    fun shuffleQueue() = queueManager.shuffleQueue()

    fun getPlaybackState() = PlaybackState(
        isPlaying = (player.playingTrack != null),
        isPaused = player.isPaused,
        position = player.playingTrack?.position ?: 0L,
        loop = loopMode.name.lowercase(),
        track = player.playingTrack?.info
    )

    // --- Queue Delegation ---

    fun getQueue(): List<AudioTrackInfo> = queueManager.getQueue()

    fun getHistory(): List<AudioTrackInfo> = queueManager.getHistory()

    fun removeQueuedTrack(index: Int) = queueManager.removeQueuedTrack(index)

    fun moveQueuedTrack(uri: String, old: Int, new: Int) = queueManager.moveQueuedTrack(uri, old, new)

    // --- Audio Event Listeners ---

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
        onTrackStartHook?.invoke()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        queueManager.addTrackHistory(track)

        if (endReason.mayStartNext) {
            if (loopMode == LoopMode.TRACK) {
                player.startTrack(track.makeClone(), false)
                return
            }
            if (loopMode == LoopMode.QUEUE) {
                queueManager.addTrack(track.makeClone())
            }
            playNextTrack()
        }
        onTrackEndHook?.invoke()
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long) {
        onTrackStuckHook?.invoke()
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) {
        onTrackException?.invoke()
    }

}