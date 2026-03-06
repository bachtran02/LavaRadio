package dev.bachtran.lavaradio.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class ClientConfig {

    @Bean
    fun restClientBuilder(): RestClient.Builder {
        return RestClient.builder()
    }
}