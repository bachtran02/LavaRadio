import com.google.protobuf.gradle.id

plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.5"
}

group = "dev.bachtran"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
	maven(url = "https://maven.lavalink.dev/releases")
	maven(url ="https://maven.topi.wtf/releases")
}

extra["springGrpcVersion"] = "1.0.1"

dependencies {
	implementation("io.grpc:grpc-services")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.grpc:spring-grpc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("dev.arbjerg:lavaplayer:2.2.6")
	implementation("dev.lavalink.youtube:v2:1.17.0")
	implementation("com.github.topi314.lavasrc:lavasrc:4.8.1")
	implementation("com.github.topi314.lavasrc:lavasrc-protocol:4.8.1")

}

dependencyManagement {
	imports {
		mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
	}
}

sourceSets {
	main {
		java {
			srcDirs("src/main/kotlin", "build/generated/source/proto/main/grpc", "build/generated/source/proto/main/java")
		}
		proto {
			srcDir("src/main/proto/webrtc-proto")
		}
	}
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java"
		}
	}
	generateProtoTasks {
		all().forEach {
			it.plugins {
				id("grpc") {
					option("@generated=omit")
				}
			}
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
