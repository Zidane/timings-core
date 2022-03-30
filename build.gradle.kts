import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin")
    id("org.spongepowered.gradle.vanilla")
    id("org.cadixdev.licenser")
    id("com.github.johnrengelman.shadow")
}

group = "org.inspirenxe"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/") {
        name = "sponge"
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.2")
    implementation("org.spongepowered:timings:1.1-SNAPSHOT") {
        isTransitive = false
    }
}

license {
    properties {
        this["name"] = "timings-core"
        this["organization"] = "InspireNXE"
        this["url"] = "https://inspirenxe.org"
    }
    header(rootProject.file("HEADER.txt"))

    include("**/*.java")
    newLine(false)
}

minecraft {
    version("1.16.5")
}

sponge {
    apiVersion("8.0.0")
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("timings") {
        displayName("Timings for Sponge")
        entrypoint("org.inspirenxe.timings.core.Timings")
        description("Aikar's Timings Platform, for Sponge")
        links {
            source("https://github.com/InspireNXE/timings-core/source")
            issues("https://github.com/InspireNXE/timings-core/issues")
        }
        contributor("Zidane") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
}

tasks {
    jar {
        archiveBaseName.set(project.name.toLowerCase())
        archiveClassifier.set("nonshaded")
    }

    shadowJar {
        dependsOn(jar)
        mergeServiceFiles()
        archiveBaseName.set(project.name.toLowerCase())
        archiveClassifier.set("")

        manifest {
            attributes(
                mapOf(
                    "MixinConfigs" to "mixins.timings.json"
                )
            )
        }
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}