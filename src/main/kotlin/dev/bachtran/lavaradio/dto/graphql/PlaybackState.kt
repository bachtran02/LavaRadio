package dev.bachtran.lavaradio.dto.graphql

data class PlaybackState(
    val isPlaying: Boolean? = null,
    val isPaused: Boolean? = null,
    val position: Double? = null,
    val loop: String? = null,
    val track: TrackInfo? = null
) {
    companion object {
        fun from(state: dev.bachtran.lavaradio.dto.rest.PlaybackState): PlaybackState {
            return PlaybackState(
                isPlaying = state.isPlaying,
                isPaused = state.isPaused,
                position = state.position.toDouble(),
                loop = state.loop,
                track = state.track?.let { TrackInfo.from(it) }
            )
        }
    }
}
