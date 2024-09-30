package com.flare.gradle.configuration

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property


/*
 * Project: gradle-plugin
 * Created at: 29/9/24 15:36
 * Created by: Dani-error
 */
interface FlareExtension {
    val entrypoint: Property<String>
    val name: Property<String>
    val description: Property<String>
    val version: Property<String>
    val website: Property<String>
    val authors: ListProperty<String>
    val dependencies: ListProperty<String>
    val optionalDependencies: ListProperty<String>
    val platforms: MapProperty<FlarePlatformType, String>
    val sdkVersion: Property<String>
}

data class FlareConfiguration(
    val entrypoint: String,
    val name: String,
    val description: String,
    val version: String,
    val website: String,
    val authors: List<String>,
    val dependencies: List<String>,
    val optionalDependencies: List<String>,
    val platforms: Set<FlarePlatform>,
    val sdkVersion: String
)

data class FlarePlatform(val type: FlarePlatformType, val dependency: String)

enum class FlarePlatformType(val displayName: String, val repository: String) {
    SPIGOT("Spigot", "https://hub.spigotmc.org/nexus/content/repositories/snapshots"),
    BUNGEECORD("BungeeCord", "https://oss.sonatype.org/content/repositories/snapshots"),
    VELOCITY("Velocity", "https://repo.papermc.io/repository/maven-public/")
}
