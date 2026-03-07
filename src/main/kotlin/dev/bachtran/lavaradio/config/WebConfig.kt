package dev.bachtran.lavaradio.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/api/stream/create", "/api/auth/current").authenticated()
                auth.anyRequest().permitAll()
            }
            .oauth2Login { oauth2 ->
                oauth2.defaultSuccessUrl("https://radio.bachtran.dev/", true)
            }
            .logout { logout ->
                logout.logoutUrl("/api/auth/logout")
                logout.logoutSuccessUrl("/").permitAll()
            }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
        return http.build()
    }
}