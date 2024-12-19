import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val spigotVersion = "1.21.4-R0.1-SNAPSHOT"
val spigotAPIVersion = "1.21"
val kotlinVersion = "2.1.0"
val configurateVersion = "4.1.2"
val mongoDriverVersion = "5.2.1"
val essentialsVersion = "2.20.1"
val vaultVersion = "1.7.1"
val inputAPIHash = "e0422fa"

plugins {
    `java-library`
    id("org.ajoberstar.grgit") version ("5.3.0")
    kotlin("jvm") version "2.1.0"
}

group = "me.daniel"
version = "${getHash()}-dev"

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://repo.essentialsx.net/releases/")
    maven(url = "https://jitpack.io")
    maven(url = "https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
    compileOnly("org.spongepowered:configurate-yaml:$configurateVersion")
    compileOnly("net.essentialsx:EssentialsX:$essentialsVersion")
    compileOnly("com.github.MilkBowl:VaultAPI:$vaultVersion")
    implementation("org.mongodb:mongodb-driver-sync:$mongoDriverVersion")
    implementation(files("libs/InputAPI-$inputAPIHash-dev.jar"))
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
            "mongo_version" to mongoDriverVersion
        )
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_23)
    }
}