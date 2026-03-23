package eu.qabatz.gradle.plugins

import eu.qabatz.gradle.JooqExtension
import eu.qabatz.gradle.Versions
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

/**
 * Convention plugin for database access with jOOQ code generation.
 * Provides: jOOQ + HikariCP + Flyway + PostgreSQL JDBC driver + DDL-based code generation.
 *
 * Code generation uses jOOQ's DDLDatabase to parse Flyway SQL migrations directly,
 * without requiring a live database. Configure via the `jooq {}` extension:
 *
 * ```kotlin
 * jooq {
 *     packageName.set("eu.qabatz.myservice.generated")
 * }
 * ```
 *
 * If `packageName` is not set, code generation is skipped and only runtime
 * dependencies are provided (backward compatible with dependency-only usage).
 */
class JooqPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(LibraryPlugin::class.java)

        val ext = configureExtension(project)
        val jooqTools = configureDependencies(project)
        val generateJooq = configureCodeGenTask(project, ext, jooqTools)
        configureSourceSets(project, generateJooq)
    }

    private fun configureExtension(project: Project): JooqExtension {
        val ext = project.extensions.create("jooq", JooqExtension::class.java)
        ext.migrationDir.convention(
            project.layout.projectDirectory.dir("src/main/resources/db/migration")
        )
        ext.excludes.convention("flyway_schema_history")
        return ext
    }

    private fun configureDependencies(
        project: Project
    ): org.gradle.api.artifacts.Configuration {
        val jooqTools = project.configurations.create("jooqCodegen")

        project.dependencies.add("jooqCodegen", "org.jooq:jooq-codegen:${Versions.JOOQ}")
        project.dependencies.add(
            "jooqCodegen",
            "org.jooq:jooq-meta-extensions:${Versions.JOOQ}",
        )

        project.dependencies.apply {
            add("implementation", "org.jooq:jooq:${Versions.JOOQ}")
            add("implementation", "org.jooq:jooq-kotlin:${Versions.JOOQ}")
            add("implementation", "org.jooq:jooq-kotlin-coroutines:${Versions.JOOQ}")
            add("implementation", "com.zaxxer:HikariCP:${Versions.HIKARI}")
            add("implementation", "org.flywaydb:flyway-core:${Versions.FLYWAY}")
            add("implementation", "org.flywaydb:flyway-database-postgresql:${Versions.FLYWAY}")
            add("implementation", "org.postgresql:postgresql:${Versions.POSTGRESQL_JDBC}")
        }
        return jooqTools
    }

    private fun configureCodeGenTask(
        project: Project,
        ext: JooqExtension,
        jooqTools: org.gradle.api.artifacts.Configuration,
    ): org.gradle.api.tasks.TaskProvider<JavaExec> {
        val outputDir = project.layout.buildDirectory.dir("generated-src/jooq/main")

        val generateJooq =
            project.tasks.register("generateJooq", JavaExec::class.java)
        generateJooq.configure(Action<JavaExec> {
            val javaExecTask = this
            group = "jooq"
            description = "Generates jOOQ classes from Flyway SQL migrations"
            classpath = jooqTools
            mainClass.set("org.jooq.codegen.GenerationTool")
            inputs.dir(ext.migrationDir)
            inputs.property("packageName", ext.packageName)
            inputs.property("excludes", ext.excludes)
            outputs.dir(outputDir)
            onlyIf { ext.packageName.isPresent }
            doFirst(Action<Task> {
                val migrationDir = ext.migrationDir.get().asFile
                val outDir = outputDir.get().asFile
                val configFile =
                    project.layout.buildDirectory.file("jooq/jooq-config.xml").get().asFile
                configFile.parentFile.mkdirs()
                outDir.mkdirs()
                configFile.writeText(
                    buildJooqConfig(
                        scripts = "${migrationDir.absolutePath}/*.sql",
                        packageName = ext.packageName.get(),
                        directory = outDir.absolutePath,
                        excludes = ext.excludes.get(),
                    )
                )
                javaExecTask.args = listOf(configFile.absolutePath)
            })
        })
        return generateJooq
    }

    private fun configureSourceSets(
        project: Project,
        generateJooq: org.gradle.api.tasks.TaskProvider<JavaExec>,
    ) {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        sourceSets.named("main").configure(Action<SourceSet> {
            java.srcDir(project.layout.buildDirectory.dir("generated-src/jooq/main"))
        })

        project.tasks.named("compileKotlin").configure(Action<Task> {
            dependsOn(generateJooq)
        })
        project.tasks.named("compileJava").configure(Action<Task> {
            dependsOn(generateJooq)
        })
    }

    companion object {
        internal fun buildJooqConfig(
            scripts: String,
            packageName: String,
            directory: String,
            excludes: String,
        ): String =
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-3.20.0.xsd">
  <generator>
    <database>
      <name>org.jooq.meta.extensions.ddl.DDLDatabase</name>
      <properties>
        <property>
          <key>scripts</key>
          <value>$scripts</value>
        </property>
        <property>
          <key>sort</key>
          <value>flyway</value>
        </property>
        <property>
          <key>defaultNameCase</key>
          <value>lower</value>
        </property>
        <property>
          <key>parseDialect</key>
          <value>POSTGRES</value>
        </property>
        <property>
          <key>parseIgnoreComments</key>
          <value>true</value>
        </property>
      </properties>
      <excludes>$excludes</excludes>
    </database>
    <target>
      <packageName>$packageName</packageName>
      <directory>$directory</directory>
    </target>
    <generate>
      <deprecated>false</deprecated>
      <records>true</records>
      <fluentSetters>true</fluentSetters>
    </generate>
  </generator>
</configuration>"""
    }
}
