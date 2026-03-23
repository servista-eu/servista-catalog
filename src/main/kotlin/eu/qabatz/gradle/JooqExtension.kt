package eu.qabatz.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class JooqExtension {
    /** Target package name for generated jOOQ classes. Must be set to enable code generation. */
    abstract val packageName: Property<String>

    /** Directory containing Flyway SQL migration files. Default: src/main/resources/db/migration. */
    abstract val migrationDir: DirectoryProperty

    /** Pattern of table names to exclude from code generation. Default: flyway_schema_history. */
    abstract val excludes: Property<String>
}
