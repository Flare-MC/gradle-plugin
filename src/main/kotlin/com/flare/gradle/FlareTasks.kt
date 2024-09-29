package com.flare.gradle

import com.flare.gradle.configuration.FlareConfiguration
import com.flare.gradle.configuration.FlarePlatformType
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File


/*
 * Project: gradle-plugin
 * Created at: 29/9/24 15:42
 * Created by: Dani-error
 */
fun generatePlatformFiles(project: Project, configuration: FlareConfiguration) {
    println(configuration)

    // Ensure the resources directory is set
    val resourcesDir = File(project.buildDir, "generated/resources")

    if (!resourcesDir.exists()) {
        resourcesDir.mkdirs()
    }

    for (platform in configuration.platforms) {
        if (platform.type == FlarePlatformType.SPIGOT || platform.type == FlarePlatformType.BUNGEECORD) {
            // YAML generation logic
            val options = DumperOptions().apply {
                defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                indent = 2
            }
            val yaml = Yaml(options)

            val pluginConfig = mutableMapOf(
                "main" to configuration.entrypoint.substringBeforeLast(".") + ".platform." + platform.type.displayName + "Entry",
                "name" to configuration.name,
                "version" to configuration.version,
                "description" to configuration.description,
                "website" to configuration.website,
                "authors" to configuration.authors.toMutableList(),
                "softdepend" to configuration.optionalDependencies.toMutableList(),
                "depend" to configuration.dependencies.toMutableList()
            )

            val configFile = File(resourcesDir, "${if (platform.type == FlarePlatformType.SPIGOT) "plugin" else "bungee"}.yml")

            configFile.writer().use { writer ->
                yaml.dump(pluginConfig, writer)
            }
        }
    }
}
