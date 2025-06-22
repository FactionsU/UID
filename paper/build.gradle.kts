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
}

tasks.runServer {
  minecraftVersion("1.21.4")
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

  archiveClassifier = ""
  archiveVersion = ""
  archiveBaseName = "factionsuuid"
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}
