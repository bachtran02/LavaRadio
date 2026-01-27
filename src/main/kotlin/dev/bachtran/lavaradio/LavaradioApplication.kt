package dev.bachtran.lavaradio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class LavaradioApplication

fun main(args: Array<String>) {
	runApplication<LavaradioApplication>(*args)
}
