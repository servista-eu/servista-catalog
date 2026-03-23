package eu.qabatz.gradle.plugins

import eu.qabatz.gradle.Versions
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for services that interact with secret stores (OpenBao, Vault).
 * Provides: commons-secrets port interfaces + OpenBao adapter + in-memory test doubles.
 */
class SecretsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(LibraryPlugin::class.java)

        project.repositories.maven {
            name = "QabatzForgejo"
            setUrl("https://git.hestia-ng.eu/api/packages/qabatz/maven")
            credentials {
                username = System.getenv("FORGEJO_USER") ?: "token"
                password = System.getenv("FORGEJO_TOKEN") ?: ""
            }
        }

        project.dependencies.apply {
            add(
                "implementation",
                "eu.qabatz:qabatz-kotlin-commons-secrets:${Versions.QABATZ_COMMONS_SECRETS}"
            )
            add(
                "testImplementation",
                project.dependencies.create(
                    "eu.qabatz:qabatz-kotlin-commons-secrets:${Versions.QABATZ_COMMONS_SECRETS}"
                ).apply {
                    (this as org.gradle.api.artifacts.ExternalModuleDependency)
                        .capabilities {
                            requireCapability("eu.qabatz:qabatz-kotlin-commons-secrets-test-fixtures")
                        }
                }
            )
        }
    }
}
