package com.flare.gradle

import com.flare.gradle.configuration.FlareConfiguration
import com.flare.gradle.configuration.FlarePlatformType
import org.gradle.api.Project
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File


/*
 * Project: gradle-plugin
 * Created at: 29/9/24 15:42
 * Created by: Dani-error
 */
fun generateResources(project: Project, configuration: FlareConfiguration) {
    // Ensure the resources directory is set
    val resourcesDir = File(project.buildDir, "generated/resources")

    if (!resourcesDir.exists()) {
        resourcesDir.mkdirs()
    }

    for (platform in configuration.platforms) {
        val main = configuration.entrypoint.substringBeforeLast(".") + ".platform." + platform.type.displayName + "Entry"
        if (platform.type == FlarePlatformType.SPIGOT || platform.type == FlarePlatformType.BUNGEECORD) {
            // YAML generation logic
            val options = DumperOptions().apply {
                defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                indent = 2
            }
            val yaml = Yaml(options)

            val pluginConfig = mutableMapOf(
                "main" to main,
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

fun generateCode(project: Project, configuration: FlareConfiguration) {
    for (platform in configuration.platforms) {
        val main =
            configuration.entrypoint.substringBeforeLast(".") + ".platform." + platform.type.displayName + "Entry"
        // Generate Kotlin file
        val generatedDir =
            File(project.buildDir, "generated/sources/" + main.replace(".", "/").substringBeforeLast("/"))
        generatedDir.mkdirs()

        val `package` = main.substringBeforeLast(".")
        val className = "${platform.type.displayName}Entry"
        val entryFile = File(generatedDir, "$className.java")
        if (platform.type == FlarePlatformType.SPIGOT || platform.type == FlarePlatformType.BUNGEECORD) {
            entryFile.writeText(
                """
                    package $`package`;

                    import ${if (platform.type == FlarePlatformType.BUNGEECORD) "net.md_5.bungee.api.plugin.Plugin" else "org.bukkit.plugin.java.JavaPlugin"};
                    import com.flare.sdk.platform.${platform.type.displayName}Platform;
                    import com.flare.sdk.Flare;

                    public class $className extends ${if (platform.type == FlarePlatformType.SPIGOT) "Java" else ""}Plugin implements ${platform.type.displayName}Platform {

                        @Override
                        public void onLoad() {
                            Flare.getInstance().setPlatform(this);
                            Flare.getInstance().onLoad();
                        }

                        @Override
                        public void onEnable() {
                            Flare.getInstance().onEnable();
                        }

                        @Override
                        public void onDisable() {
                            Flare.getInstance().onDisable();
                        }
                    }
                """.trimIndent()
            )
        } else {
            val dependencies = mutableListOf<String>()
            for (dependency in configuration.dependencies) {
                dependencies.add("@Dependency(id = $dependency, optional = false)")
            }
            for (dependency in configuration.optionalDependencies) {
                dependencies.add("@Dependency(id = $dependency, optional = true)")
            }

            entryFile.writeText(
                """
                    package $`package`;

                    import com.google.inject.Inject;
                    import com.velocitypowered.api.plugin.Plugin;
                    import com.velocitypowered.api.proxy.ProxyServer;
                    import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
                    import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
                    import org.slf4j.Logger;
                    import com.flare.sdk.platform.VelocityPlatform;
                    import com.flare.sdk.Flare;

                    @Plugin(id = "${configuration.name}", name = "${configuration.name}", version = "${configuration.version}", description = "${configuration.description}", url = "${configuration.website}", dependencies = ${dependencies.joinToString(prefix = "{", postfix = "}", separator = ", ") })
                    public class $className implements VelocityPlatform {

                        private final ProxyServer server;
                        private final Logger logger;

                        @Inject
                        public $className(ProxyServer server, Logger logger) {
                            this.server = server;
                            this.logger = logger;
                        }

                        @Subscribe
                        public void onProxyInitialization(ProxyInitializeEvent event) {
                            Flare.getInstance().setPlatform(this);
                            Flare.getInstance().onLoad();
                            Flare.getInstance().onEnable();
                        }

                        @Subscribe
                        public void onProxyShutdown(ProxyShutdownEvent event) {
                            Flare.getInstance().onDisable();
                        }
                    }
                """.trimIndent()
            )
        }
    }
}
