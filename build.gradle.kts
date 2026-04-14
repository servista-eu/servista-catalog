plugins {
    `maven-publish`
    `version-catalog`
}

// The shared jvm-library CI pipeline invokes `test` and `detekt`. This project
// is a pure version catalog with no sources, so register them as no-ops.
tasks.register("test") { group = "verification" }

tasks.register("detekt") { group = "verification" }

// Published version catalog -- loaded from catalog/ directory.
// Consumers import via: from("eu.servista:servista-catalog:x.y.z")
catalog {
    versionCatalog {
        from(files("catalog/libs.versions.toml"))
    }
}

// Publishing configuration -- version catalog to Forgejo Maven registry
publishing {
    publications {
        create<MavenPublication>("versionCatalog") {
            from(components["versionCatalog"])
            artifactId = "servista-catalog"
        }
    }

    repositories {
        maven {
            name = "Forgejo"
            val repoUrl =
                findProperty("publishUrl")?.toString()
                    ?: "https://git.hestia-ng.eu/api/packages/servista/maven"
            url = uri(repoUrl)
            isAllowInsecureProtocol = repoUrl.startsWith("http://")
            credentials {
                username =
                    findProperty("forgejoUser")?.toString()
                        ?: System.getenv("FORGEJO_USER")
                        ?: "token"
                password =
                    findProperty("publishToken")?.toString()
                        ?: findProperty("forgejoToken")?.toString()
                        ?: System.getenv("FORGEJO_TOKEN")
                        ?: ""
            }
        }
    }
}
