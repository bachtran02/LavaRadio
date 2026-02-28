package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.dto.rest.StreamState
import dev.bachtran.lavaradio.service.StreamManagerService
import dev.bachtran.lavaradio.utils.githubId
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stream")
class StreamController(
    private val streamManagerService: StreamManagerService
) {
    @GetMapping("/guest")
    fun getGuestStreamState(): StreamState {
        return streamManagerService.getStreamState("guest")
    }

    @GetMapping("/user")
    fun getUserStreamState(@AuthenticationPrincipal user: OAuth2User): StreamState {
        return streamManagerService.getStreamState(user.githubId)
    }

    @PostMapping("/create/guest")
    fun createGuestStream(): String {
        return streamManagerService.createStream("guest")
    }

    @PostMapping("/create/user")
    fun startUserStream(@AuthenticationPrincipal user: OAuth2User): String {
        return streamManagerService.createStream(user.githubId)
    }
}