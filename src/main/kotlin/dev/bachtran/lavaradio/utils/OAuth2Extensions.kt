package dev.bachtran.lavaradio.utils

import org.springframework.security.oauth2.core.user.OAuth2User

val OAuth2User.githubId: String
    get() = this.attributes["id"]?.toString()
        ?: throw IllegalStateException("GitHub ID not found in provider attributes")