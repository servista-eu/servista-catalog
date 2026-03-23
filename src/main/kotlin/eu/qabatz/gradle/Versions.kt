package eu.qabatz.gradle

/**
 * Centralized version constants used by Qabatz convention plugins.
 *
 * Only versions referenced by plugin code belong here. Each constant MUST match
 * the corresponding entry in the published version catalog (catalog/libs.versions.toml).
 * When bumping a version, update BOTH this file and the catalog.
 */
object Versions {
    const val KTOR = "3.4.0"
    const val KOIN = "4.1.1"
    const val JOOQ = "3.20.11"
    const val FLYWAY = "12.0.3"
    const val HIKARI = "7.0.2"
    const val POSTGRESQL_JDBC = "42.7.10"
    const val KAFKA = "4.1.1"
    const val MICROMETER = "1.16.3"
    const val KOTLIN_LOGGING = "7.0.3"
    const val LOGBACK = "1.5.32"
    const val KOTLINX_COROUTINES = "1.10.2"
    const val KOTLINX_DATETIME = "0.7.1"
    const val KOTLINX_SERIALIZATION = "1.10.0"
    const val JUNIT5 = "5.14.2"
    const val MOCKK = "1.14.7"
    const val TESTCONTAINERS = "2.0.3"
    const val KOTEST = "6.1.4"
    const val QABATZ_COMMONS_SECRETS = "0.1.1"
}
