import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "com.italiarevenge"
version = project.property("pluginVersion") as String
description = "IR-SellChest (AutoSellChests fork)"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.decentholograms.eu/")
    maven("https://repo.fancyplugins.de/releases")
}

dependencies {
    // Paper API — provided at runtime
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    // Vault — provided by Vault plugin at runtime
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // IR-Shop — provided at runtime (local JAR)
    compileOnly(files("libs/IR-Shop-1.7.0.jar"))

    // AnvilGUI pre-compiled NMS wrappers — must be bundled (cannot be recompiled)
    implementation(files("libs/anvilgui-compiled.jar"))

    // Optional hologram/AFK hook classes (pre-compiled from original plugin, bundled at runtime)
    // Their source files are excluded below since the API JARs are not on public Maven repos.
    implementation(files("libs/optional-hooks.jar"))

    // CMI is not on a public Maven repo — place CMI-API.jar in libs/ to enable CMI support
    if (file("libs/CMI-API.jar").exists()) {
        compileOnly(files("libs/CMI-API.jar"))
    }
}

// Exclude source files whose APIs are not on public Maven repos (pre-compiled in optional-hooks.jar)
tasks.named<JavaCompile>("compileJava") {
    exclude(
        "**/AFKDetectionCMI.java",
        "**/AFKDetectionEssentials.java",
        "**/CMIHologramHook.java",
        "**/DecentHologramHook.java",
        "**/FancyHologramHook.java",
        "**/FakeHologramHook.java"
    )
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        mergeServiceFiles()
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        exclude("META-INF/versions/*/module-info.class")
    }

    build {
        dependsOn("shadowJar")
    }

    runServer {
        minecraftVersion("1.21.11")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
