group = "me.cjcrafter"
version = "1.0.0"

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

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/WeaponMechanics/MechanicsMain")
        credentials {
            username = "CJCrafter"
            password = "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" // this is a public token created in CJCrafter's name which will never expire
        }
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")

    api("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")
    implementation("co.aikar:minecraft-timings:1.0.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    compileOnly("me.deecaad:mechanicscore:+") // consider replacing with the latest version
    compileOnly("me.deecaad:weaponmechanics:+") // consider replacing with the latest version

    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.0.6")
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
    archiveFileName.set("WeaponMechanicsCosmetics-${project.version}.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        relocate ("co.aikar.timings.lib", "me.deecaad.weaponmechanicscosmetics.libs.timings") {
            include(dependency("co.aikar:minecraft-timings"))
        }
        relocate ("org.mariuszgromada.math", "me.deecaad.weaponmechanicscosmetics.libs.math") {
            include(dependency("org.mariuszgromada.math:MathParser.org-mXparser"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

bukkit {
    main = "me.deecaad.weaponmechanicscosmetics.WeaponMechanicsCosmeticsLoader"
    name = "WeaponMechanicsCosmetics"
    apiVersion = "1.13"

    authors = listOf("CJCrafter", "DeeCaaD")
    softDepend = listOf("MechanicsCore", "WeaponMechanics")
}