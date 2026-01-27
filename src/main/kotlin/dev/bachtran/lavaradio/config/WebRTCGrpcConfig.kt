package dev.bachtran.lavaradio.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "webrtcgrpc")
data class WebRTCGrpcConfig(
    val host: String,
    val port: Int
)
