plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

repositories {
    mavenLocal()

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")

    api("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")
    implementation("co.aikar:minecraft-timings:1.0.4")

    compileOnly(files(file("libs/MechanicsCore-1.0.1-BETA.jar")))
    compileOnly(files(file("libs/WeaponMechanics-1.0.1-BETA.jar")))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(8)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    classifier = null;
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        relocate ("co.aikar.timings.lib", "me.deecaad.weaponmechanicscosmetics.timingslib") {
            include(dependency("co.aikar:minecraft-timings"))
        }
    }
}

bukkit {
    main = "me.deecaad.weaponmechanicscosmetics.WeaponMechanicsCosmeticsLoader"
    name = "WeaponMechanicsCosmetics"
    apiVersion = "1.13"

    authors = listOf("DeeCaaD", "CJCrafter")
    softDepend = listOf("MechanicsCore", "WeaponMechanics")
}

group = "me.deecaad"
version = "1.0.0"