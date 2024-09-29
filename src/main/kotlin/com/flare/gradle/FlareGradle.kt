package com.flare.gradle

import com.flare.gradle.configuration.FlareConfiguration
import com.flare.gradle.configuration.FlareExtension
import com.flare.gradle.configuration.FlarePlatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import java.io.File


/*
 * Project: gradle-plugin
 * Created at: 29/9/24 15:33
 * Created by: Dani-error
 */
class FlareGradle : Plugin<Project> {

    /**
     * [Plugin.apply]
     *
     * @param project The target project.
     */
    override fun apply(project: Project) {
        val extension = project.extensions.create<FlareExtension>("flare")


        // Register the generatePlatformFiles task
        project.tasks.register("generatePlatformFiles") {
            doLast {
                val entryPoint = extension.entrypoint.orNull ?: throw UnsupportedOperationException("The entry point is not defined in the 'flare' block.")
                val name = extension.name.getOrElse(project.name)
                val description = extension.description.getOrElse("")
                val version = extension.version.getOrElse(project.version.toString())
                val website = extension.website.getOrElse("")
                val authors = extension.authors.getOrElse(listOf())
                val dependencies = extension.dependencies.getOrElse(listOf())
                val optionalDependencies = extension.optionalDependencies.getOrElse(listOf())
                val platforms = extension.platforms.getOrElse(mapOf())
                val sdkVersion = extension.sdkVersion.orNull ?: throw UnsupportedOperationException("The sdk version is not defined in the 'flare' block.")

                if (platforms.isEmpty())
                    throw UnsupportedOperationException("The platforms are not defined in the 'flare' block.")

                val configuration = FlareConfiguration(
                    entryPoint,
                    name,
                    description,
                    version,
                    website,
                    authors,
                    dependencies,
                    optionalDependencies,
                    platforms.map { FlarePlatform(it.key, it.value) }.toSet(),
                    sdkVersion
                )

                project.getClass(entryPoint) ?:
                    throw UnsupportedOperationException("The entry point class doesn't exists!")

                generatePlatformFiles(project, configuration)
            }
        }
        project.tasks.withType(Jar::class.java) {
            from(File(project.buildDir, "generated/resources")) {
                include("**/*")
            }
        }

        // Ensure the generation task runs before build
        project.tasks.named("build") {
            dependsOn("generatePlatformFiles")
        }

    }

}

fun Project.getClass(className: String): File? {
    val javaPluginConvention = this.convention.getPlugin(JavaPluginConvention::class.java)
    val mainSourceSet = javaPluginConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

    // Get the compiled class output directory
    val outputDir = mainSourceSet.allJava.srcDirs

    // Convert the class name to a file path (e.g., "com.example.MyClass" -> "com/example/MyClass.class")
    val classPath = className.replace('.', '/')


    for (dir in outputDir) {
        val javaFile = dir.resolve("$classPath.java")
        val kotlinFile = dir.resolve("$classPath.kt")

        if (javaFile.exists()) return javaFile

        if (kotlinFile.exists()) return kotlinFile
    }

    return null
}

