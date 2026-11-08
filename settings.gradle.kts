pluginManagement {
    repositories {
        maven { url = uri("https://maven.leclowndu93150.dev/releases") }
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.neoforged.net/releases") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    }
}

plugins {
    id("dev.prism.settings") version "0.6.4"
}

rootProject.name = "Thaumaturge-Tinkers"

prism {
    version("26.1.2") {
        neoforge()
        atRoot()
    }
}
