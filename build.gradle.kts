import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "me.cjcrafter"
version = "3.2.0"

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

bukkit {
    main = "me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics"
    name = "WeaponMechanicsCosmetics"
    apiVersion = "1.13"

    authors = listOf("CJCrafter", "DeeCaaD")
    depend = listOf("ProtocolLib", "MechanicsCore", "WeaponMechanics")
    softDepend = listOf("VivecraftSpigot")
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
    compileOnly("org.jetbrains:annotations:24.0.1")

    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    compileOnly("net.kyori:adventure-api:4.15.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.1")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.15.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.15.0")

    compileOnly("com.cjcrafter:mechanicscore:1.0.0")
    compileOnly("com.cjcrafter:weaponmechanics:1.0.0")
    compileOnly("com.cjcrafter:vivecraft:3.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    implementation("org.bstats:bstats-bukkit:3.0.1")
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.2.1")
}

tasks.named<ShadowJar>("shadowJar") {
    classifier = null;
    archiveFileName.set("WeaponMechanicsCosmetics-${project.version}.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        relocate ("co.aikar.timings.lib", "me.cjcrafter.weaponmechanicscosmetics.lib.timings") {
            include(dependency("co.aikar:minecraft-timings"))
        }
        relocate ("org.mariuszgromada.math", "me.cjcrafter.weaponmechanicscosmetics.lib.math") {
            include(dependency("org.mariuszgromada.math:MathParser.org-mXparser"))
        }
        relocate ("org.bstats", "me.cjcrafter.weaponmechanicscosmetics.lib.bstats") {
            include(dependency("org.bstats:"))
        }

        // Relocate to MechanicsCore adventure locations, so we use their shaded version
        relocate("net.kyori", "me.deecaad.core.lib")
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
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