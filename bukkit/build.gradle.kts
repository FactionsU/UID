plugins {
  alias(libs.plugins.indra)
  alias(libs.plugins.shadow)
}

indra {
  javaVersions {
    target(21)
  }
}

configurations.implementation {
  exclude("com.google.code.findbugs", "jsr305")
}

dependencies {
  compileOnlyApi(libs.jspecify)

  implementation(libs.cloud.paper)
  implementation(libs.cloud.extras)
  implementation(libs.commons.lang3)
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
      "version" to project.version,
      "adv" to libs.versions.adventure.get(),
      "apiversion" to libs.versions.apiversion.get()
    ))
  }
}

tasks.shadowJar {
  dependencies {
    include(dependency("com.typesafe:config"))
    include(dependency("org.spongepowered:configurate-core"))
    include(dependency("org.spongepowered:configurate-hocon"))
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
  relocate("ninja.leaping", "moss.factions.shade.ninja.leaping")
  relocate("org.incendo", "moss.factions.shade.org.incendo")
  relocate("io.leangen", "moss.factions.shade.io.leangen")
  relocate("com.github.stefvanschie.inventoryframework", "moss.factions.shade.stefvanschie.if")
  relocate("com.ezylang", "moss.factions.shade.com.ezylang")

  relocate("org.apache.commons.codec", "moss.factions.shade.org.apache.commons.codec")
  relocate("org.apache.commons.logging", "moss.factions.shade.org.apache.commons.logging")
  relocate("org.apache.http", "moss.factions.shade.org.apache.http")

  archiveClassifier = ""
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}
