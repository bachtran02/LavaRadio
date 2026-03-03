package dev.bachtran.lavaradio.utils

import org.springframework.security.oauth2.core.user.OAuth2User

val OAuth2User.spotifyId: String
    get() = this.attributes["id"]?.toString() ?: "unknown_id"

val OAuth2User.displayName: String
    get() = this.attributes["display_name"]?.toString() ?: "Spotify User"