package dev.bachtran.lavaradio.dto.rest

data class StreamState(
    val identifier : String = "",
    val existed: Boolean = false,
    val active: Boolean = false,
)