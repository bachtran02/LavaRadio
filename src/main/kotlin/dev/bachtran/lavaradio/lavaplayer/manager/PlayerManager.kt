package dev.bachtran.lavaradio.lavaplayer.manager

import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager
import com.github.topi314.lavasrc.deezer.DeezerAudioTrack.TrackFormat
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver
import com.github.topi314.lavasrc.spotify.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import dev.bachtran.lavaradio.lavaplayer.config.LavaplayerConfig
import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.lavalink.youtube.clients.AndroidVrWithThumbnail
import dev.lavalink.youtube.clients.MusicWithThumbnail
import dev.lavalink.youtube.clients.WebEmbeddedWithThumbnail
import dev.lavalink.youtube.clients.WebWithThumbnail
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.Future
import java.util.function.Function

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class PlayerManager(private val lavaplayerConfig: LavaplayerConfig) {

    val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    /* We need to initialize this before creating beans */
    @PostConstruct
    fun setup() {
        val spotify = SpotifySourceManager(
            lavaplayerConfig.sources.spotify.clientId,
            lavaplayerConfig.sources.spotify.clientSecret,
            lavaplayerConfig.sources.spotify.spDc,
            lavaplayerConfig.sources.spotify.countryCode,
            Function { audioPlayerManager },
            DefaultMirroringAudioTrackResolver(lavaplayerConfig.providers.toTypedArray()),
        )

        val deezer = DeezerAudioSourceManager(
            lavaplayerConfig.sources.deezer.masterDecryptionKey,
            lavaplayerConfig.sources.deezer.arl,
            arrayOf(TrackFormat.MP3_128, TrackFormat.MP3_64)
        )

        val youtube = YoutubeAudioSourceManager(
            true, true, true,
            MusicWithThumbnail(), AndroidVrWithThumbnail(), WebWithThumbnail(), WebEmbeddedWithThumbnail()
        )

        val soundcloud = SoundCloudAudioSourceManager.createDefault()

        /* Register external sources */
        audioPlayerManager.registerSourceManager(youtube)
        audioPlayerManager.registerSourceManager(spotify)
        audioPlayerManager.registerSourceManager(deezer)
        audioPlayerManager.registerSourceManager(soundcloud)

        /* Register local source */
        AudioSourceManagers.registerLocalSource(audioPlayerManager)

        audioPlayerManager.configuration.outputFormat = lavaplayerConfig.getAudioDataFormat()
        audioPlayerManager.configuration.resamplingQuality = AudioConfiguration.ResamplingQuality.MEDIUM
    }

    fun createPlayer(): AudioPlayer = audioPlayerManager.createPlayer()

    fun loadItem(identifier: String, handler: AudioLoadResultHandler): Future<Void?>? = audioPlayerManager.loadItem(identifier, handler)

    fun loadItemSync(identifier: String): AudioItem? = audioPlayerManager.loadItemSync(identifier)
}