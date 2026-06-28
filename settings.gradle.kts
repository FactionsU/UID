plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "factionsuuid"

include("annotation-processor")
include("bukkit")
include("paper")
include("example-plugin")
