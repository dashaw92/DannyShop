import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    `java-library`
    id("org.ajoberstar.grgit") version ("5.0.0")
    kotlin("jvm") version "1.8.0"
}

group = "me.daniel"
version = "${getHash()}-dev"
val spigotVersion = "1.19.3-R0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://repo.essentialsx.net/releases/")
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("net.essentialsx:EssentialsX:2.19.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    implementation(files("libs/InputAPI-e0422fa-dev.jar"))
    implementation(kotlin("stdlib"))
}

fun getHash() = grgit.head().abbreviatedId ?: "unknown"

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("git_commit" to version)
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "18"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "18"
}