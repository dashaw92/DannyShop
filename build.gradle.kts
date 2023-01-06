plugins {
    `java-library`
    id("org.ajoberstar.grgit") version("5.0.0")
}

group = "me.daniel"
version = "${getHash()}-dev"
val spigotVersion = "1.19.3-R0.1-SNAPSHOT"

//tasks.withType<JavaCompile> {
//    options.compilerArgs.add("--enable-preview")
//}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    maven ( url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/" )
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
}

fun getHash() = grgit.head().abbreviatedId ?: "unknown"

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("git_commit" to version)
    }
}