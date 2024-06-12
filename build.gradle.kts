plugins {
  alias(libs.plugins.indra)
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
      includeModule("org.kitteh", "paste-gg-api")
    }
  }

  exclusiveContent {
    forRepository {
      maven("https://jitpack.io/")
    }

    filter {
      includeModule("com.github.MilkBowl", "VaultAPI")
    }
  }
}

configurations.implementation {
  exclude("com.google.code.findbugs", "jsr305")
}

dependencies {
  compileOnlyApi(libs.jspecify)

  implementation(libs.paper.lib)
  implementation(libs.commons.lang3)
  implementation(libs.commodore)
  implementation(libs.configurate.hocon) {
    exclude("com.google.inject", "guice")
    exclude("org.checkerframework", "checker-qual")
  }
  implementation(libs.adventure.platform.bukkit) {
    exclude("org.jetbrains", "annotations")
  }
  implementation(libs.adventure.text.minimessage) {
    exclude("org.jetbrains", "annotations")
  }
  implementation(libs.spigot) {
    exclude("net.md-5", "bungeecord-chat")
  }
  implementation(libs.authlib) {
    exclude("com.google.code.findbugs", "jsr305")
    exclude("com.google.code.gson", "gson")
    exclude("com.google.guava", "guava")
    exclude("org.apache.commons", "commons-lang3")
  }

  listOf(
    libs.vault.api,
    libs.worldedit.core,
    libs.worldedit.bukkit,
    libs.worldguard.core,
    libs.worldguard.bukkit,
    libs.essentialsx.asProvider(),
    libs.essentialsx.chat,
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
    libs.graves
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

tasks.shadowJar {
  dependencies {
    include(dependency("io.papermc:paperlib"))
    include(dependency("me.lucko:commodore"))
    include(dependency("org.spongepowered:configurate-hocon"))
  }

  relocate("com.typesafe", "moss.factions.shade.com.typesafe")
  relocate("io.papermc.lib", "moss.factions.shade.io.papermc.lib")
  relocate("me.lucko.commodore", "moss.factions.shade.me.lucko.commodore")
  relocate("ninja.leaping", "moss.factions.shade.ninja.leaping")

  relocate("net.kyori", "moss.factions.shade.net.kyori")
  relocate("org.apache.commons.codec", "moss.factions.shade.org.apache.commons.codec")
  relocate("org.apache.commons.logging", "moss.factions.shade.org.apache.commons.logging")
  relocate("org.apache.http", "moss.factions.shade.org.apache.http")
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}
