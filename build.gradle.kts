plugins {
    `maven-publish`
    `version-catalog`
}

// Published version catalog -- loaded from catalog/ directory.
// Consumers import via: from("eu.qabatz:qabatz-catalog:x.y.z")
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
            artifactId = "qabatz-gradle-plugins-catalog"
        }
    }

    repositories {
        maven {
            name = "Forgejo"
            val repoUrl =
                findProperty("publishUrl")?.toString()
                    ?: "https://git.hestia-ng.eu/api/packages/qabatz/maven"
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
