package dev.bachtran.lavaradio.controller

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import dev.bachtran.lavaradio.dto.rest.PlaybackState
import dev.bachtran.lavaradio.service.StreamManagerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/private")
class PersonalController(
    private val streamManagerService: StreamManagerService
) {
    @GetMapping("/{userId}")
    fun getMyPlaybackState(@PathVariable userId: String): PlaybackState {
        val myStreamId = streamManagerService.getUserStreamId(userId)
        return streamManagerService.withRadio(myStreamId) { it.getPlaybackState() }
    }
}