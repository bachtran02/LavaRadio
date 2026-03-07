package dev.bachtran.lavaradio.utils

import org.springframework.security.oauth2.core.user.OAuth2User

val OAuth2User.githubId: String
    get() = this.attributes["id"]?.toString() ?: "unknown_id"

val OAuth2User.githubName: String
    get() = this.attributes["name"]?.toString() ?: "GitHub User"