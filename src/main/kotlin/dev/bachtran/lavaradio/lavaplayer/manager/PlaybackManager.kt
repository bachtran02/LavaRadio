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
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@Component
class PlaybackManager(private val player: AudioPlayer) : AudioEventAdapter() {
    companion object {
        private const val PLAYBACK_QUEUE_SIZE = 5
        private const val HISTORY_QUEUE_SIZE = 20
    }

    private val lock = Any()
    private val masterQueueList = Collections.synchronizedList<AudioTrack>(ArrayList());
    private val playbackQueue = LinkedBlockingQueue<AudioTrack>(PLAYBACK_QUEUE_SIZE)
    private val historyQueue = LinkedBlockingQueue<AudioTrack>(HISTORY_QUEUE_SIZE)

    @PostConstruct
    fun setup() {
        player.addListener(this)
    }

    fun addTrack(track: AudioTrack) {

        synchronized(lock) {
            masterQueueList.add(track)
            syncPlaybackQueue()

            if (player.playingTrack == null && playbackQueue.isNotEmpty()) {
                playNextInternal()
            }
        }
    }

    fun addTracks(tracks: List<AudioTrack>) {

        if (tracks.isEmpty()) return

        synchronized(lock) {
            masterQueueList.addAll(tracks)
            syncPlaybackQueue()

            if (player.playingTrack == null && playbackQueue.isNotEmpty()) {
                playNextInternal()
            }
        }
    }

    fun playTrack(track: AudioTrack) {
        player.startTrack(track, false)
    }

    fun playNextTrack() {
        synchronized(lock) {
            playNextInternal()
        }
    }

    fun togglePause(isPaused: Boolean) {
        player.isPaused = isPaused
    }

    fun stop() {

        synchronized(lock) {
            masterQueueList.clear()
            playbackQueue.clear()
        }
        historyQueue.clear()
        player.stopTrack()
    }

    fun seek(position: Long) {
        player.playingTrack?.position = position
    }

    fun getPlaybackState(): PlaybackState {
        return PlaybackState(
            isPlaying = (player.playingTrack != null),
            isPaused = player.isPaused,
            position = player.playingTrack?.position ?: 0L,
            track = player.playingTrack?.info
        )
    }

    fun removeQueuedTrack(index: Int): Boolean {

        if (index < 0) return false

        synchronized(lock) {
            if (index >= masterQueueList.size) {
                return false
            }

            masterQueueList.removeAt(index)
            syncPlaybackQueue()
        }
        return true
    }

    fun moveQueuedTrack(trackUri: String, oldIndex: Int, newIndex: Int): Boolean {

        if (oldIndex < 0 || newIndex < 0) return false

        synchronized(lock) {
            if (oldIndex >= masterQueueList.size || newIndex >= masterQueueList.size) {
                return false
            }

            val trackToMove = masterQueueList[oldIndex]
            if (trackToMove.info.uri != trackUri) return false

            masterQueueList.removeAt(oldIndex)
            masterQueueList.add(newIndex, trackToMove)
            syncPlaybackQueue()
        }
        return true
    }

    fun getQueue() : List<AudioTrackInfo> {
        synchronized(lock) {
            return masterQueueList.map { it.info }
        }
    }

    fun getHistory() : List<AudioTrackInfo> {
        return historyQueue.toList().reversed().map { it.info }
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {}

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {

        if (historyQueue.size == HISTORY_QUEUE_SIZE) {
            historyQueue.poll()
        }
        historyQueue.offer(track)

        if (endReason.mayStartNext) {
            playNextTrack()
        }
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long) { /* TODO: */}

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) { /* TODO: */ }

    private fun syncPlaybackQueue() {
        /* NOTE: ensure we are holding masterQueueLock */
        playbackQueue.clear()
        playbackQueue.addAll(masterQueueList.take(PLAYBACK_QUEUE_SIZE))
    }

    private fun playNextInternal() {

        synchronized(lock) {

            val nextTrack = playbackQueue.poll()

            check(masterQueueList.firstOrNull() === nextTrack) {
                "Queue Inconsistency: Expected ${nextTrack.info.uri} at head of list, " +
                        "but found ${masterQueueList.firstOrNull()?.info?.uri}"
            }

            /* nextTrack is nullable, if so skip the current track */
            player.startTrack(nextTrack, false)

            if (nextTrack != null ) {
                masterQueueList.removeAt(0)
                syncPlaybackQueue()
            }
        }
    }
}