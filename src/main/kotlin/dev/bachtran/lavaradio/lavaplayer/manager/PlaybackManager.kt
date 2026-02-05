package dev.bachtran.lavaradio.lavaplayer.manager

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.bachtran.lavaradio.common.PlaybackState
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue

@Component
class PlaybackManager(private val player: AudioPlayer) : AudioEventAdapter() {
    private val queue = LinkedBlockingQueue<AudioTrack>()

    @PostConstruct
    fun setup() {
        player.addListener(this)
    }

    fun addTrack(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    fun playTrack(track: AudioTrack) {
        player.startTrack(track, false)
    }

    fun playNextTrack() {
        player.startTrack(queue.poll(), false)
    }

    fun togglePause(isPaused: Boolean) {
        player.isPaused = isPaused
    }

    fun stop() {
        queue.clear()
        player.stopTrack()
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
        val track = queue.elementAtOrNull(index) ?: return false
        return queue.remove(track)
    }

    fun getQueue() = queue.map { it.info }.toList()

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {}

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) { playNextTrack() }
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long) { /* TODO: */}

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) { /* TODO: */ }

}