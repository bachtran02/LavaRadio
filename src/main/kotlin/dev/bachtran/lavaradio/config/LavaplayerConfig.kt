package dev.bachtran.lavaradio.config

import com.sedmelluq.discord.lavaplayer.format.OpusAudioDataFormat
import org.springframework.context.annotation.Configuration
import com.sedmelluq.discord.lavaplayer.format.Pcm16AudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lavaplayer")
data class LavaplayerConfig(
    val sources: SourcesConfig,
    val providers: List<String>,
) {

    companion object {
        const val SAMPLE_RATE = 48000
        const val CHANNELS = 2
        const val CHUNK_SAMPLE_COUNT = 960
    }

    fun getAudioDataFormat() = OpusAudioDataFormat(
        CHANNELS, SAMPLE_RATE, CHUNK_SAMPLE_COUNT
    )
}

data class SourcesConfig(
    val spotify: SpotifyConfig,
    val deezer: DeezerConfig
)

data class SpotifyConfig(
    val clientId: String,
    val clientSecret: String,
    val countryCode: String,
    val spDc: String
)

data class DeezerConfig(
    val masterDecryptionKey: String,
    val arl: String,
    val formats: List<String> = emptyList() // Defaulting to an empty list
)

