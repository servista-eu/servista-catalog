package eu.qabatz.gradle.plugins

import eu.qabatz.gradle.Versions
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

/**
 * Convention plugin for test dependencies.
 * Provides: JUnit 5 + Testcontainers + MockK + Kotest assertions + Ktor test host + Koin test.
 */
class TestingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(LibraryPlugin::class.java)

        project.dependencies.apply {
            add("testImplementation", "org.junit.jupiter:junit-jupiter:${Versions.JUNIT5}")
            add("testImplementation", "io.mockk:mockk:${Versions.MOCKK}")
            add("testImplementation", "io.kotest:kotest-assertions-core:${Versions.KOTEST}")
            add("testImplementation", "org.testcontainers:testcontainers:${Versions.TESTCONTAINERS}")
            add("testImplementation", "org.testcontainers:testcontainers-postgresql:${Versions.TESTCONTAINERS}")
            add("testImplementation", "org.testcontainers:testcontainers-kafka:${Versions.TESTCONTAINERS}")
            add("testImplementation", "io.ktor:ktor-server-test-host:${Versions.KTOR}")
            add("testImplementation", "io.insert-koin:koin-test:${Versions.KOIN}")
        }

        project.tasks.withType(Test::class.java).configureEach(Action<Test> {
            useJUnitPlatform()
        })
    }
}
