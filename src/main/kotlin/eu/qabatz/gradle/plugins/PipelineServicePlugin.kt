package eu.qabatz.gradle.plugins

import eu.qabatz.gradle.Versions
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Kafka Streams pipeline services.
 */
class PipelineServicePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(LibraryPlugin::class.java)
        project.dependencies.add("implementation", "org.apache.kafka:kafka-streams:${Versions.KAFKA}")
    }
}
