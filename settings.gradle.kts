rootProject.name = "timings-core"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
        }
    }

    plugins {
        id("org.spongepowered.gradle.plugin") version "2.0.1"
        id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
        id("org.cadixdev.licenser") version "0.6.1"
        id("com.github.johnrengelman.shadow") version "7.0.0"
    }
}