package dev.bachtran.lavaradio.service.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.bachtran.lavaradio.dto.PlaybackStateDTO
import dev.bachtran.lavaradio.dto.TrackInfoDTO
import java.util.concurrent.LinkedBlockingQueue
import javax.sound.midi.Track

class PlaybackManager(private val player: AudioPlayer) : AudioEventAdapter() {
    private val queue = LinkedBlockingQueue<AudioTrack>()

    fun addTrack(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    fun playNextTrack() {
        player.startTrack(queue.poll(), false)
    }

    fun stop() {
        queue.clear()
        player.stopTrack()
    }

    fun togglePause(isPaused: Boolean) {
        player.isPaused = isPaused
    }

    fun getCurrentTrack(): AudioTrack? = player.playingTrack

    fun getQueue(): List<AudioTrack> = queue.toList()

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {}

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) { playNextTrack() }
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long) { /* TODO: */}

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) { /* TODO: */ }

}