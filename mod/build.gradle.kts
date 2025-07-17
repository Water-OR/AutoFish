import dev.architectury.pack200.java.Pack200Adapter
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("gg.essential.loom") version "1.9.31"
    id("com.gradleup.shadow") version "8.3.7"
    `java-library`
    `kotlin-dsl`
}

val modVersion = properties["version"] as String
val tweakerClass = "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker"

val mixinConfig = "mixin.wor.auto-fish.json"
val mixinRefmap = "mixin.refmap.wor.auto-fish.json"

val atFile = run<_> lookup@{
    for (dir in sourceSets.main.get().resources.srcDirs) for (f in dir.walkBottomUp())
        if (f.isFile && f.name.endsWith("_at.cfg")) return@lookup f
    return@lookup null
}

version = modVersion
group = "net.llvg"
base.archivesName = "Auto Fish"

loom {
    runConfigs {
        "client" {
            property("mixin.debug.export", "true")
            property("mixin.debug.verbose", "true")
            programArgs("--tweakClass", tweakerClass)
            isIdeConfigGenerated = true
        }
    }
    
    forge {
        pack200Provider = Pack200Adapter()
        mixinConfig(mixinConfig)
        atFile?.let { accessTransformer(it) }
    }
    
    @Suppress("UnstableApiUsage")
    mixin.defaultRefmapName = mixinRefmap
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
        vendor = JvmVendorSpec.AZUL
    }
    
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    
    withSourcesJar()
}

val shade by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = true
}

configurations {
    implementation.extendsFrom(shade)
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public") {
        name = "Spongepowered Repo"
        content {
            includeGroup("org.spongepowered")
        }
    }
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        name = "DevAuth Repo"
        content {
            includeGroup("me.djtheredstoner")
        }
    }
    maven("https://repo.polyfrost.org/releases") {
        name = "PolyFrost Repo"
        content {
            includeGroup("cc.polyfrost")
        }
    }
}

@Suppress("UnstableApiUsage")
dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
    
    modCompileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.2-alpha+")
    modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
    shade("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta17")
    
    compileOnly("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

tasks {
    processResources {
        inputs.property("refmap", mixinRefmap)
        filesMatching(mixinConfig) { expand("refmap" to mixinRefmap) }
        rename("(.+_at.cfg)", "META-INF/$1")
    }
    
    shadowJar {
        archiveClassifier = "dev"
        configurations = listOf(shade.get())
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        
        mergeServiceFiles()
    }
    
    remapJar {
        archiveClassifier = ""
        inputFile = shadowJar.get().archiveFile
        dependsOn(shadowJar)
    }
    
    jar {
        mutableMapOf<String, Any>().also { map ->
            map["ForceLoadAsMod"] = true
            map["ModSide"] = "CLIENT"
            map["TweakClass"] = tweakerClass
            map["TweakOrder"] = -1
            atFile?.let { map["FMLAT"] = it.name }
        }.let(manifest::attributes)
        
        finalizedBy(shadowJar)
        enabled = false
    }
}