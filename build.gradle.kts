plugins {
    `kotlin-dsl`
    `maven-publish`
    java
}

val projectName: String by project
val projectVersion: String by project
val projectGroup: String by project

group = projectGroup
version = projectVersion

kotlin.jvmToolchain(8)

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>()
    .configureEach {
        compilerOptions
            .languageVersion
            .set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
    }

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Flare Gradle Plugin")
                description.set("Flare Flamework's gradle plugin")
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

dependencies {
    // SnakeYAML for YAML file handling
    implementation("org.yaml:snakeyaml:2.3")

    // Gradle API dependencies
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("flare") {
            id = projectGroup
            displayName = projectName
            version = version
            description =
                "A multi-platform Minecraft plugin framework to target multiple platforms with just a single code-base."
            implementationClass = "$projectGroup.FlareGradle"
        }
    }
}

tasks.named("build") {
    finalizedBy("publishToMavenLocal")
}
