plugins {
  alias(libs.plugins.indra)
  alias(libs.plugins.runPaper)
  alias(libs.plugins.shadow)
}

indra {
  github("FactionsU", "UID")

  javaVersions {
    target(21)
  }
}

// Repositories are checked in order.
repositories {
  mavenCentral()

  exclusiveContent {
    forRepository {
      maven("https://repo.papermc.io/repository/maven-public/")
    }

    filter {
      includeModule("io.papermc", "paperlib")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    }

    filter {
      includeModule("org.spigotmc", "spigot-api")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://libraries.minecraft.net/")
    }

    filter {
      includeGroup("com.mojang")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://maven.enginehub.org/repo/")
    }

    filter {
      includeGroup("com.sk89q")
      includeGroup("com.sk89q.lib")
      includeGroup("com.sk89q.worldedit")
      includeGroup("com.sk89q.worldedit.worldedit-libs")
      includeGroup("com.sk89q.worldguard")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://repo.essentialsx.net/releases/")
    }

    filter {
      includeGroup("net.essentialsx")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    filter {
      includeModule("me.clip", "placeholderapi")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://ci.ender.zone/plugin/repository/everything/")
    }

    filter {
      includeModule("com.drtshock", "PlayerVaultsX")
      includeModule("com.griefcraft.lwc", "Modern-LWC")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://repo.citizensnpcs.co")
    }

    filter {
      includeGroup("com.denizenscript")
      includeGroup("net.citizensnpcs")
      includeGroup("org.mcmonkey")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://repo.mikeprimm.com/")
    }

    filter {
      includeGroup("org.dynmap")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://repo.ranull.com/maven/ranull/")
    }

    filter {
      includeGroup("com.ranull")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://github.com/factions-site/repo/raw/public")
    }

    filter {
      includeModule("be.maximvdw", "MVdWPlaceholderAPI")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://jitpack.io/")
    }

    filter {
      includeModule("com.github.MilkBowl", "VaultAPI")
      includeModule("com.github.dumbo-the-developer.Duels", "duels-api")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://dependency.download/releases")
    }

    filter {
      includeModule("dev.kitteh.forkedproject", "IF-ChestOnly")
    }
  }
}

configurations.implementation {
  exclude("com.google.code.findbugs", "jsr305")
}

dependencies {
  compileOnlyApi(libs.jspecify)

  implementation(libs.paper.lib)
  implementation(libs.cloud.paper)
  implementation(libs.cloud.extras)
  implementation(libs.commons.lang3)
  implementation(libs.commodore)
  implementation(libs.configurate.hocon) {
    exclude("com.google.inject", "guice")
    exclude("org.checkerframework", "checker-qual")
  }
  implementation(libs.adventure.text.minimessage) {
    exclude("org.jetbrains", "annotations")
  }
  implementation(libs.adventure.text.serializer.gson) {
    exclude("org.jetbrains", "annotations")
  }
  implementation(libs.adventure.text.serializer.legacy) {
    exclude("org.jetbrains", "annotations")
  }
  implementation(libs.spigot)
  implementation(libs.authlib) {
    exclude("com.google.code.findbugs", "jsr305")
    exclude("com.google.code.gson", "gson")
    exclude("com.google.guava", "guava")
    exclude("org.apache.commons", "commons-lang3")
  }
  implementation(libs.ifchestonly)
  implementation(libs.evalex)

  listOf(
    libs.vault.api,
    libs.worldedit.core,
    libs.worldedit.bukkit,
    libs.worldguard.core,
    libs.worldguard.bukkit,
    libs.essentialsx,
    libs.fastutil,
    libs.dynmap,
    libs.playervaultsx,
    libs.placeholderapi,
    libs.mvdwplaceholderapi,
    libs.modernlwc,
    libs.sentinel,
    libs.denizen,
    libs.citizens,
    libs.depenizen,
    libs.luckperms.api,
    libs.magic.api,
    libs.graves,
    libs.duels
  ).forEach {
    compileOnly(it) {
      isTransitive = false
    }
  }

  testImplementation(platform("org.junit:junit-bom:5.10.2"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.processResources {
  filesMatching("plugin.yml") {
    expand(mapOf(
      "version" to project.version
    ))
  }
}

tasks.runServer {
  minecraftVersion("1.21.4")
}

tasks.shadowJar {
  dependencies {
    include(dependency("com.typesafe:config"))
    include(dependency("io.papermc:paperlib"))
    include(dependency("me.lucko:commodore"))
    include(dependency("org.spongepowered:configurate-core"))
    include(dependency("org.spongepowered:configurate-hocon"))
    include(dependency("net.kyori:adventure-api"))
    include(dependency("net.kyori:adventure-key"))
    include(dependency("net.kyori:adventure-nbt"))
    include(dependency("net.kyori:adventure-platform-api"))
    include(dependency("net.kyori:adventure-platform-facet"))
    include(dependency("net.kyori:adventure-platform-bukkit"))
    include(dependency("net.kyori:adventure-text-logger-slf4j"))
    include(dependency("net.kyori:adventure-text-minimessage"))
    include(dependency("net.kyori:adventure-text-serializer-ansi"))
    include(dependency("net.kyori:adventure-text-serializer-bungeecord"))
    include(dependency("net.kyori:adventure-text-serializer-gson"))
    include(dependency("net.kyori:adventure-text-serializer-gson-legacy-impl"))
    include(dependency("net.kyori:adventure-text-serializer-json"))
    include(dependency("net.kyori:adventure-text-serializer-json-legacy-impl"))
    include(dependency("net.kyori:adventure-text-serializer-legacy"))
    include(dependency("net.kyori:adventure-text-serializer-plain"))
    include(dependency("net.kyori:examination-api"))
    include(dependency("net.kyori:examination-string"))
    include(dependency("net.kyori:option"))
    include(dependency("org.incendo:cloud-paper"))
    include(dependency("org.incendo:cloud-minecraft-extras"))
    include(dependency("org.incendo:cloud-bukkit"))
    include(dependency("org.incendo:cloud-brigadier"))
    include(dependency("org.incendo:cloud-core"))
    include(dependency("org.incendo:cloud-services"))
    include(dependency("io.leangen.geantyref:geantyref"))
    include(dependency("dev.kitteh.forkedproject:IF-ChestOnly"))
    include(dependency("com.ezylang:EvalEx"))
  }

  relocate("com.typesafe", "moss.factions.shade.com.typesafe")
  relocate("io.papermc.lib", "moss.factions.shade.io.papermc.lib")
  relocate("me.lucko.commodore", "moss.factions.shade.me.lucko.commodore")
  relocate("ninja.leaping", "moss.factions.shade.ninja.leaping")
  relocate("org.incendo", "moss.factions.shade.org.incendo")
  relocate("io.leangen", "moss.factions.shade.io.leangen")
  relocate("com.github.stefvanschie.inventoryframework", "moss.factions.shade.stefvanschie.if")
  relocate("com.ezylang", "moss.factions.shade.com.ezylang")

  relocate("net.kyori", "moss.factions.shade.net.kyori")
  relocate("org.apache.commons.codec", "moss.factions.shade.org.apache.commons.codec")
  relocate("org.apache.commons.logging", "moss.factions.shade.org.apache.commons.logging")
  relocate("org.apache.http", "moss.factions.shade.org.apache.http")
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}
