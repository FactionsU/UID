plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "factionsuuid"

include("bukkit")
include("paper")
include("example-plugin")
