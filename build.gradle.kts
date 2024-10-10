group = "me.cjcrafter"
version = "3.4.2"

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

bukkit {
    main = "me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics"
    name = "WeaponMechanicsCosmetics"
    apiVersion = "1.13"
    foliaSupported = true

    authors = listOf("CJCrafter", "DeeCaaD")
    depend = listOf("ProtocolLib", "MechanicsCore", "WeaponMechanics")
    softDepend = listOf("VivecraftSpigot")
}

repositories {
    mavenLocal()

    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://repo.maven.apache.org/maven2/")
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")

    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    compileOnly("net.kyori:adventure-api:4.15.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.1")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.15.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.15.0")

    compileOnly("com.cjcrafter:mechanicscore:3.4.13-FOLIA")
    compileOnly("com.cjcrafter:weaponmechanics:3.4.14-FOLIA")
    compileOnly("com.cjcrafter:vivecraft:3.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("com.github.cryptomorin:XSeries:11.0.0")

    implementation("org.bstats:bstats-bukkit:3.0.1")
    implementation("com.cjcrafter:foliascheduler:0.4.7")
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.2.1")
}

tasks.shadowJar {
    archiveFileName.set("WeaponMechanicsCosmetics-${project.version}.jar")

    dependencies {
        relocate("co.aikar.timings.lib", "me.cjcrafter.weaponmechanicscosmetics.lib.timings") {
            include(dependency("co.aikar:minecraft-timings"))
        }
        relocate("org.mariuszgromada.math", "me.cjcrafter.weaponmechanicscosmetics.lib.math") {
            include(dependency("org.mariuszgromada.math:MathParser.org-mXparser"))
        }
        relocate("org.bstats", "me.cjcrafter.weaponmechanicscosmetics.lib.bstats") {
            include(dependency("org.bstats:"))
        }

        // Relocate to MechanicsCore adventure locations, so we use their shaded version
        relocate("net.kyori", "me.deecaad.core.lib")
        relocate("com.cryptomorin.xseries", "me.deecaad.core.lib.xseries")
        relocate("com.cjcrafter.foliascheduler", "me.deecaad.core.lib.scheduler")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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