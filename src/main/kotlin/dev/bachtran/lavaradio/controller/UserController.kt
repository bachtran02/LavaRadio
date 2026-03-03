package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.utils.displayName
import dev.bachtran.lavaradio.utils.spotifyId
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class UserController {

    @GetMapping("/current")
    fun getAuthUser(@AuthenticationPrincipal user: OAuth2User): Map<String, String> {
        return mapOf(
            "name" to user.displayName,
            "id" to user.spotifyId
        )
    }
}