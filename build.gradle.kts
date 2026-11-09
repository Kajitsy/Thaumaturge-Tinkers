import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("dev.prism")
}

fun prop(name: String): String = providers.gradleProperty(name).get()

val minecraftVersion = prop("minecraft_version")

group = prop("mod_group_id")
version = prop("mod_version")

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://maven.leclowndu93150.dev/releases") }
}

prism {
    curseMaven()
    modrinthMaven()
    maven("BlameJared", "https://maven.blamejared.com/")
    maven("IllusiveSoulworks", "https://maven.theillusivec4.top/")
    maven("Shedaniel", "https://maven.shedaniel.me/")
    maven("Architectury", "https://maven.architectury.dev/")

    metadata {
        modId = prop("mod_id")
        name = prop("mod_name")
        description = prop("mod_description")
        license = prop("mod_license")
        prop("mod_authors").split(",").forEach { author(it.trim()) }
        expand("minecraft_version_range", prop("minecraft_version_range"))
        expand("neoforge_version_range", prop("neo_version_range"))
    }

    version(minecraftVersion) {
        parchmentMinecraftVersion = prop("parchment_minecraft_version")
        parchmentMappingsVersion = prop("parchment_mappings_version")

        neoforge {
            loaderVersion = prop("neo_version")

            dependencies {
                implementation("com.leclowndu93150:thaumaturge-1.21.1-neoforge:1.0.12")

                compileOnly("top.theillusivec4.curios:curios-neoforge:${prop("curios_version")}+$minecraftVersion:api")
                runtimeOnly("top.theillusivec4.curios:curios-neoforge:${prop("curios_version")}+$minecraftVersion")

                compileOnly("mezz.jei:jei-$minecraftVersion-common-api:${prop("jei_version")}")
                compileOnly("mezz.jei:jei-$minecraftVersion-neoforge-api:${prop("jei_version")}")

                compileOnly("maven.modrinth:jade:${prop("jade_version")}")
                runtimeOnly("maven.modrinth:jade:${prop("jade_version")}")
            }

            rawProject {
                tasks.named<ProcessResources>("processResources") {
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                }
                tasks.withType<Jar> {
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                }
                tasks.named<Jar>("jar") {
                    archiveBaseName.set("Thaumaturge-Tinkers")
                }
            }
        }
    }
}
