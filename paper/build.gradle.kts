plugins {
  alias(libs.plugins.indra)
  alias(libs.plugins.shadow)
  alias(libs.plugins.runPaper)
}

indra {
  javaVersions {
    target(21)
  }
}

dependencies {
  implementation(project(":bukkit")) {
    exclude("org.spigotmc")
  }
  compileOnly(libs.paper)
  compileOnly(libs.cloud.paper)
  compileOnly(libs.cloud.extras)
}

tasks.runServer {
  minecraftVersion(libs.versions.apiversion.get())
}

tasks.processResources {
  filesMatching("paper-plugin.yml") {
    expand(mapOf(
      "version" to project.version,
      "apiversion" to libs.versions.apiversion.get()
    ))
  }
}

tasks.compileJava {
  dependsOn(":bukkit:jar", ":bukkit:shadowJar")
}

tasks.shadowJar {
  dependsOn(":bukkit:shadowJar")
  dependencies {
    include(project(":bukkit"))
  }

  relocate("com.typesafe", "moss.factions.shade.com.typesafe")
  relocate("io.papermc.lib", "moss.factions.shade.io.papermc.lib")
  relocate("ninja.leaping", "moss.factions.shade.ninja.leaping")
  relocate("org.incendo", "moss.factions.shade.org.incendo")
  relocate("io.leangen", "moss.factions.shade.io.leangen")
  relocate("com.github.stefvanschie.inventoryframework", "moss.factions.shade.stefvanschie.if")
  relocate("com.ezylang", "moss.factions.shade.com.ezylang")

  archiveClassifier = ""
  archiveVersion = ""
  archiveBaseName = "factionsuuid"
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}
