plugins {
  alias(libs.plugins.indra)
  alias(libs.plugins.shadow)
  alias(libs.plugins.runPaper)
  id("maven-publish")
}

indra {
  javaVersions {
    target(21)
  }
}

publishing {
  publications {
    create<MavenPublication>("factionsuuid") {
      artifact(tasks.shadowJar)
      artifact(tasks.javadocJar)
      artifact(tasks.sourcesJar)
      groupId = "dev.kitteh"
      artifactId = "factions"
      version = project.version.toString()
      pom {
        name = "FactionsUUID"
        description = "Best factions plugin!"
        licenses {
          license {
            name = "GNU General Public License (GPL) version 3"
            url = "https://www.gnu.org/licenses/gpl-3.0.txt"
          }
        }
        developers {
          developer {
            id = "mbaxter"
            name = "Matt Baxter"
            email = "matt@kitteh.org"
            url = "https://www.kitteh.dev/"
            organization = "Kitteh"
            organizationUrl = "https://www.kitteh.dev"
            roles = setOf("Lead Developer", "Cat Wrangler")
          }
        }
        issueManagement {
          system = "GitHub"
          url = "https://github.com/FactionsU/UID/issues"
        }
        scm {
          connection = "scm:git:git://github.com/FactionsU/UID.git"
          developerConnection = "scm:git:git://github.com/FactionsU/UID.git"
          url = "git@github.com:FactionsU/UID.git"
        }
        repositories {
          maven {
            name = "DependencyDownload"
            val rel = "https://dependency.download/releases"
            val snap = "https://dependency.download/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snap else rel)
            credentials(PasswordCredentials::class)
          }
        }
      }
    }
  }
}

dependencies {
  implementation(project(":bukkit")) {
    exclude("org.spigotmc")
  }
  compileOnly(libs.paper)
  compileOnly(libs.bundles.cloud)
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
