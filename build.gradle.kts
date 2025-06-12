group = "me.cjcrafter"
version = "4.0.6"

plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
}

bukkitPluginYaml {
    main = "com.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics"
    name = "WeaponMechanicsCosmetics"
    apiVersion = "1.13"
    foliaSupported = true

    authors = listOf("CJCrafter", "DeeCaaD")
    depend = listOf("packetevents", "MechanicsCore", "WeaponMechanics")
    softDepend = listOf("VivecraftSpigot")
}

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://repo.maven.apache.org/maven2/")
    maven(url = "https://repo.codemc.io/repository/maven-releases/")
    maven(url = "https://repo.jeff-media.com/public/") // SpigotUpdateChecker
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")

    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    compileOnly("net.kyori:adventure-api:4.18.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.18.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.18.0")

    compileOnly("com.cjcrafter:mechanicscore:4.1.0")
    compileOnly("com.cjcrafter:weaponmechanics:4.1.0")
    compileOnly("com.cjcrafter:vivecraft:3.0.0")
    compileOnly("com.github.retrooper:packetevents-spigot:2.8.0")
    compileOnly("com.github.cryptomorin:XSeries:13.3.1")
    compileOnly("com.cjcrafter:foliascheduler:0.6.3")
    compileOnly("dev.jorel:commandapi-bukkit-core:9.7.0")
    compileOnly("org.bstats:bstats-bukkit:3.0.1")
    compileOnly("com.jeff_media:SpigotUpdateChecker:3.0.4")

    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.2.1")
}

tasks.shadowJar {
    archiveFileName.set("WeaponMechanicsCosmetics-${project.version}.jar")

    relocate("org.mariuszgromada.math", "com.cjcrafter.weaponmechanicscosmetics.lib.math")
    relocate("org.bstats", "me.deecaad.core.lib.bstats")
    relocate("net.kyori", "me.deecaad.core.lib.kyori")
    relocate("com.jeff_media.updatechecker", "me.deecaad.core.lib.updatechecker")
    relocate("com.cryptomorin.xseries", "me.deecaad.core.lib.xseries")
    relocate("com.cjcrafter.foliascheduler", "me.deecaad.core.lib.scheduler")
    relocate("dev.jorel.commandapi", "me.deecaad.core.lib.commandapi")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(21)
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