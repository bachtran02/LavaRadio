package dev.bachtran.lavaradio.lavaplayer.manager

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import dev.bachtran.lavaradio.dto.PlaybackState
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import kotlin.text.lowercase

enum class LoopMode { TRACK, NONE }

@Component
class PlaybackManager(private val player: AudioPlayer) : AudioEventAdapter() {

    private val queueManager = QueueManager()

    private val playLock = Any()

    private var loopMode = LoopMode.NONE

    @PostConstruct
    fun setup() {
        player.addListener(this)
    }

    fun addTrack(track: AudioTrack) {
        synchronized(playLock) {
            if (player.playingTrack == null) {
                playTrack(track)
            } else {
                queueManager.addTrack(track)
            }
        }
    }

    fun addTracks(tracks: List<AudioTrack>) {
        if (tracks.isEmpty()) return

        synchronized(playLock) {
            if (player.playingTrack == null) {
                playTrack(tracks[0])
                if (tracks.size > 1) {
                    queueManager.addTracks(tracks.drop(1))
                }
            } else {
                queueManager.addTracks(tracks)
            }
        }
    }

    fun playTrack(track: AudioTrack) { player.startTrack(track, false) }

    fun playNextTrack() { player.startTrack(queueManager.popNextTrack(), false) }

    fun togglePause(isPaused: Boolean) { player.isPaused = isPaused }

    fun stop() {

        queueManager.clearQueue()
        player.stopTrack()
        queueManager.clearHistory()
    }

    fun seek(position: Long) { player.playingTrack?.position = position }

    fun setLoop(mode: String) {
        loopMode = when (mode.lowercase()) {
            "none" -> LoopMode.NONE
            "track" -> LoopMode.TRACK
            else -> throw IllegalArgumentException("Invalid loop mode: $mode")
        }
    }

    fun shuffleQueue() { queueManager.shuffleQueue() }

    fun getPlaybackState(): PlaybackState {
        return PlaybackState(
            isPlaying = (player.playingTrack != null),
            isPaused = player.isPaused,
            position = player.playingTrack?.position ?: 0L,
            loop = loopMode.name.lowercase(),
            track = player.playingTrack?.info
        )
    }

    fun removeQueuedTrack(index: Int) = queueManager.removeQueuedTrack(index)

    fun moveQueuedTrack(trackUri: String, oldIndex: Int, newIndex: Int): Boolean {
        return queueManager.moveQueuedTrack(trackUri, oldIndex, newIndex)
    }

    fun getQueue() : List<AudioTrackInfo> = queueManager.getQueue()

    fun getHistory() : List<AudioTrackInfo> = queueManager.getHistory()

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {}

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {

        queueManager.addTrackHistory(track)

        if (endReason.mayStartNext) {
            if (loopMode == LoopMode.TRACK) {
                player.startTrack(track.makeClone(), false)
                return
            }
            playNextTrack()
        }
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long) { /* TODO: */}

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) { /* TODO: */ }

}