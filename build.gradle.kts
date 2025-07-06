plugins {
  alias(libs.plugins.indra)
}

indra {
  github("FactionsU", "UID")

  javaVersions {
    target(21)
  }
}

allprojects {
  // Repositories are checked in order.
  repositories {
    mavenCentral()

    exclusiveContent {
      forRepository {
        maven("https://repo.papermc.io/repository/maven-public/")
      }

      filter {
        //includeModule("io.papermc.paper", "paper-api")
        includeModule("net.md-5", "bungeecord-chat")
      }
    }

    maven("https://maven-prs.papermc.io/Paper/pr12671") {
      name = "Maven for PR #12671" // https://github.com/PaperMC/Paper/pull/12671
      mavenContent {
        includeModule("io.papermc.paper", "paper-api")
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
      }
    }

    exclusiveContent {
      forRepository {
        maven("https://maven.citizensnpcs.co/repo")
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
}
