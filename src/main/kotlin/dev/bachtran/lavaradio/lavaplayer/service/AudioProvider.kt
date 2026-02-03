package dev.bachtran.lavaradio.lavaplayer.service

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import org.springframework.stereotype.Component

@Component
class AudioProvider(private val player: AudioPlayer) {

    fun provide(): AudioFrame? {
        return player.provide()
    }
}