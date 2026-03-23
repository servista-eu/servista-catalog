package eu.qabatz.gradle.plugins

import eu.qabatz.gradle.Versions
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Base convention plugin for all Qabatz projects.
 * Provides: Kotlin 2.3/JVM 21, detekt, ktfmt, directory structure validation.
 */
class LibraryPlugin : Plugin<Project> {
    private companion object {
        const val JVM_VERSION = 21
    }

    override fun apply(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.plugins.apply("dev.detekt")
        project.plugins.apply("com.ncorti.ktfmt.gradle")

        project.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension::class.java).apply {
            jvmToolchain(JVM_VERSION)
        }

        project.tasks.register("validateProjectStructure").configure(Action<Task> {
            group = "verification"
            description = "Validates that required Qabatz directory structure exists"
            doLast(Action<Task> {
                val required = listOf("src/main/kotlin", "src/main/resources", "src/test/kotlin")
                val missing = required.filter { dir -> !project.file(dir).exists() }
                if (missing.isNotEmpty()) {
                    throw GradleException(
                        "Missing required directories: ${missing.joinToString(", ")}. " +
                            "Qabatz projects must follow the standard directory layout."
                    )
                }
            })
        })

        project.tasks.named("compileKotlin").configure(Action<Task> {
            dependsOn("validateProjectStructure")
        })

        val detektExt = project.extensions.getByType(dev.detekt.gradle.extensions.DetektExtension::class.java)
        detektExt.buildUponDefaultConfig.set(true)
        detektExt.parallel.set(true)
        val detektConfig = Thread.currentThread().contextClassLoader
            .getResource("detekt/detekt.yml")
        if (detektConfig != null) {
            detektExt.config.from(project.resources.text.fromUri(detektConfig.toURI()))
        }

        project.extensions.getByType(com.ncorti.ktfmt.gradle.KtfmtExtension::class.java).apply {
            kotlinLangStyle()
        }

        project.dependencies.add(
            "implementation",
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLINX_COROUTINES}",
        )
        project.dependencies.add(
            "implementation",
            "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.KOTLINX_DATETIME}",
        )
    }
}
