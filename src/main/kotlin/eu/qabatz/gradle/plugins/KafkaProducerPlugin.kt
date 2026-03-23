package eu.qabatz.gradle.plugins

import eu.qabatz.gradle.Versions
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for services that produce Kafka events.
 */
class KafkaProducerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(LibraryPlugin::class.java)
        project.dependencies.add("implementation", "org.apache.kafka:kafka-clients:${Versions.KAFKA}")
    }
}
