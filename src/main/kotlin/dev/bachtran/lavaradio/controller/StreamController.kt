package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.dto.rest.StreamState
import dev.bachtran.lavaradio.service.StreamManagerService
import dev.bachtran.lavaradio.utils.githubId
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stream")
class StreamController(
    private val streamManagerService: StreamManagerService
) {

    @GetMapping("/{streamId}")
    fun getUserStreamState(@PathVariable streamId: String): StreamState {
        return streamManagerService.getStreamState(streamId)
    }

    @PostMapping("/create/user")
//    fun createUserStream(@AuthenticationPrincipal user: OAuth2User): String {
    fun createUserStream(): String {
        return streamManagerService.createStream("123")
    }

    @PostMapping("/{streamId}/start")
    fun startUserStream(@PathVariable streamId: String) {
        return streamManagerService.startStream(streamId)
    }
}