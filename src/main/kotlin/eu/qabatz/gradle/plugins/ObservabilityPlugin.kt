package eu.qabatz.gradle.plugins

import eu.qabatz.gradle.Versions
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for observability.
 * Provides: kotlin-logging + Logback + Micrometer Prometheus registry.
 */
class ObservabilityPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(LibraryPlugin::class.java)

        project.dependencies.apply {
            add("implementation", "io.github.oshai:kotlin-logging-jvm:${Versions.KOTLIN_LOGGING}")
            add("implementation", "ch.qos.logback:logback-classic:${Versions.LOGBACK}")
            add("implementation", "io.micrometer:micrometer-registry-prometheus:${Versions.MICROMETER}")
        }
    }
}
