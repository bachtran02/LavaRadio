package dev.bachtran.lavaradio.service.lavaplayer

import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager
import com.github.topi314.lavasrc.deezer.DeezerAudioTrack.TrackFormat
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver
import com.github.topi314.lavasrc.spotify.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import dev.bachtran.lavaradio.config.LavaplayerConfig
import dev.bachtran.lavaradio.dto.PlaybackStateDTO
import dev.bachtran.lavaradio.dto.TrackInfoDTO
import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.lavalink.youtube.clients.AndroidVr
import dev.lavalink.youtube.clients.Music
import dev.lavalink.youtube.clients.Web
import dev.lavalink.youtube.clients.WebEmbedded
import org.springframework.stereotype.Service
import java.util.function.Function


@Service
class LavaPlayerService(lavaplayerConfig: LavaplayerConfig) {

    private val playerManager = DefaultAudioPlayerManager()

    private val lavaplayerPlayer = playerManager.createPlayer()

    private val playbackManager = PlaybackManager(lavaplayerPlayer)

    init {

        val spotify = SpotifySourceManager(
            lavaplayerConfig.sources.spotify.clientId,
            lavaplayerConfig.sources.spotify.clientSecret,
            lavaplayerConfig.sources.spotify.spDc,
            lavaplayerConfig.sources.spotify.countryCode,
            Function { playerManager },
            DefaultMirroringAudioTrackResolver(lavaplayerConfig.providers.toTypedArray()),
        )

        val deezer = DeezerAudioSourceManager(
            lavaplayerConfig.sources.deezer.masterDecryptionKey,
            lavaplayerConfig.sources.deezer.arl,
            arrayOf(TrackFormat.MP3_128, TrackFormat.MP3_64)
        )

        val youtube = YoutubeAudioSourceManager(
            true, true, true,
            Music(), AndroidVr(), Web(), WebEmbedded()
        )

        val soundcloud = SoundCloudAudioSourceManager.createDefault()

        /* Register external sources */
        playerManager.registerSourceManager(youtube)
        playerManager.registerSourceManager(spotify)
        playerManager.registerSourceManager(deezer)
        playerManager.registerSourceManager(soundcloud)
        /* Register local source */
        AudioSourceManagers.registerLocalSource(playerManager)

        playerManager.configuration.outputFormat = lavaplayerConfig.getAudioDataFormat()
        playerManager.configuration.resamplingQuality = AudioConfiguration.ResamplingQuality.MEDIUM

        lavaplayerPlayer.addListener( playbackManager )
    }

    fun provideFrame(): AudioFrame? = lavaplayerPlayer.provide()

    fun play(identifier: String) {
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

    /* Defer player actions to PlaybackManager */
    fun pause() = playbackManager.togglePause(true)
    fun resume() = playbackManager.togglePause(false)
    fun skip() = playbackManager.playNextTrack()
    fun stop() = playbackManager.stop()

    fun getPlaybackState(): PlaybackStateDTO {
        val current = playbackManager.getCurrentTrack()

        return if (current != null) {
            PlaybackStateDTO(
                isPlaying = true,
                isPaused = lavaplayerPlayer.isPaused,
                track = current.toDto()
            )
        } else {
            PlaybackStateDTO(isPlaying = false, isPaused = false, track = null)
        }
    }

    fun getQueue(): List<TrackInfoDTO> {
        return playbackManager.getQueue().map { it.toDto() }
    }

    private fun AudioTrack.toDto(): TrackInfoDTO {
        return TrackInfoDTO(
            title = this.info.title,
            author = this.info.author,
            length = this.duration,
            position = this.position,
            isStream = this.info.isStream,
            uri = this.info.uri,
        )
    }
}