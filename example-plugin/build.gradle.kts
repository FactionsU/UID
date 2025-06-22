plugins {
  alias(libs.plugins.indra)
}

indra {
  javaVersions {
    target(21)
  }
}

dependencies {
  //compileOnly(project(":bukkit"))
  compileOnly(project(":paper", configuration = "shadow"))
  compileOnly(libs.paper)
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
  dependsOn(":bukkit:shadowJar", ":paper:shadowJar")
}
