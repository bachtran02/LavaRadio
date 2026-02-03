package dev.bachtran.lavaradio.lavaplayer.config

import dev.bachtran.lavaradio.lavaplayer.manager.PlayerManager
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.bachtran.lavaradio.lavaplayer.manager.PlaybackManager
import dev.bachtran.lavaradio.lavaplayer.service.AudioProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AudioPlayerBeanConfig {

    @Bean
    fun audioPlayer(playerManager: PlayerManager): AudioPlayer = playerManager.createPlayer()

    @Bean
    fun playbackManager(player: AudioPlayer): PlaybackManager = PlaybackManager(player)

    @Bean
    fun audioProvider(player: AudioPlayer): AudioProvider = AudioProvider(player)
}