import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val spigotVersion = "26.1.2-R0.1-SNAPSHOT"
val spigotAPIVersion = "26.1.2"
val kotlinVersion = "2.4.0"
val configurateVersion = "4.1.2"
//val essentialsVersion = "2.21.2"
val vaultVersion = "1.7.1"
val inputAPIHash = "9c56b66"

plugins {
    `java-library`
    id("org.ajoberstar.grgit") version ("5.3.0")
    kotlin("jvm") version "2.4.0"
}

group = "me.daniel"
//version = "${getHash()}-dev"
version = "dev"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://repo.essentialsx.net/releases/")
    maven(url = "https://jitpack.io")
    maven(url = "https://papermc.io/repo/repository/maven-public/")
    maven(url = "https://maven.enginehub.org/repo")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
    compileOnly("org.spongepowered:configurate-yaml:$configurateVersion")
    compileOnly("com.github.MilkBowl:VaultAPI:$vaultVersion")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17")
    compileOnly("com.github.GriefPrevention:GriefPrevention:18.0.0")
    implementation(kotlin("stdlib"))
}

fun getHash() = grgit.head().abbreviatedId ?: "unknown"

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand(
            "git_commit" to version,
            "kotlin_version" to kotlinVersion,
            "spigot_api" to spigotAPIVersion,
            "configurate_version" to configurateVersion,
        )
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}