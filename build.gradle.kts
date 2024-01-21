import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.ajoberstar.grgit") version ("5.2.1")
    kotlin("jvm") version "1.9.22"
}

group = "me.daniel"
version = "${getHash()}-dev"
val spigotVersion = "1.20.4-R0.1-SNAPSHOT"

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
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    implementation("org.mongodb:mongodb-driver-sync:4.8.2")
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
    jvmTarget = "21"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "21"
}