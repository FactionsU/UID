plugins {
    alias(libs.plugins.indra)
}

indra {
    javaVersions {
        target(25)
    }
}

dependencies {
    compileOnly(project(":paper", configuration = "shadow"))
    compileOnly(libs.paper)

    compileOnly(libs.apiguardian)
    compileOnly(libs.immutables.value.annotations)
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand(
            mapOf(
                "version" to project.version,
                "apiversion" to libs.versions.apiversion.get()
            )
        )
    }
}

tasks.compileJava {
    dependsOn(":bukkit:shadowJar", ":paper:shadowJar")
}
