// distribution: distributable
//   the version catalog every consumer of the distributable set resolves. ⛔ It therefore names
//   ONLY distributable coordinates: the 25 `commons-infra-*` aliases left it at 0.3.0
//   (`F419 #595`) because a catalog a contractor holds is an inventory a contractor reads.
// See DISTRIBUTION.md. Asserted by `node tools/check-distribution-boundary.mjs` in servista-planning.

import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    `maven-publish`
    `version-catalog`
}

// The shared jvm-library CI pipeline invokes `test` and `detekt`. This project
// is a pure version catalog with no sources, so register them as no-ops.
// ⛔ F359 #534: a no-op `test` would also never compile or run a `@Test` someone adds here, the
// non-void `@Test` trap's worst case: every case silently NOT RUN and the gate green. With no
// JUnit Platform to set `junit.platform.discovery.issue.severity.critical` on, the guard is this:
// any file under `src/` turns `test` red. Census 2026-10-16: 0 classes, 0 `@Test` methods, no
// Kotlin or Java plugin applied, so nothing that could not be compiled.
tasks.register("test") {
    group = "verification"
    val sources = fileTree("src")
    val catalogRoot = layout.projectDirectory.asFile
    inputs.files(sources)
    doLast {
        val found = sources.files
        if (found.isNotEmpty()) {
            throw GradleException(
                "servista-catalog has no source set, so `test` runs nothing; these files would " +
                    "never be compiled or run (F359 #534):\n" +
                    found.joinToString("\n") { "  ${it.relativeTo(catalogRoot)}" }
            )
        }
    }
}

tasks.register("detekt") { group = "verification" }

// Published version catalog -- loaded from catalog/ directory.
// Consumers import via: from("eu.servista:servista-catalog:x.y.z")
catalog {
    versionCatalog {
        from(files("catalog/libs.versions.toml"))
    }
}

// F978 #978 D1: archives are byte-reproducible by declaration, so a half-published release can be
// re-published identical. The catalog's only publication file is the generated TOML, which no
// archive task produces, so today this reaches no task; it holds for any archive added later.
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
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
