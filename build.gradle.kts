plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    `version-catalog`
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// detekt for this project -- use the bundled shared config
detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(files("src/main/resources/detekt/detekt.yml"))
}

dependencies {
    // External Gradle plugins that convention plugins apply via plugins {} blocks.
    // Each convention script that uses `kotlin("jvm")`, `id("dev.detekt")`, etc.
    // needs the corresponding plugin artifact on the classpath.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.ktfmt.gradle.plugin)
    implementation(libs.kotlinx.serialization.json)

    // TestKit + test dependencies
    testImplementation(gradleTestKit())
    testImplementation(libs.junit5)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.kotest.assertions)
}

// Published version catalog -- loaded from catalog/ directory.
// Consumers import via: from("eu.qabatz:qabatz-gradle-plugins-catalog:x.y.z")
catalog {
    versionCatalog {
        from(files("catalog/libs.versions.toml"))
    }
}

// Functional test source set for Gradle TestKit.
// Must be defined before gradlePlugin {} references it.
val functionalTest by sourceSets.creating {
    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
}

val functionalTestImplementation by configurations.getting {
    extendsFrom(configurations["testImplementation"])
}
val functionalTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations["testRuntimeOnly"])
}

// Register convention plugins. All 8 plugins registered here.
gradlePlugin {
    plugins {
        create("library") {
            id = "qabatz.library"
            implementationClass = "eu.qabatz.gradle.plugins.LibraryPlugin"
        }
        create("testing") {
            id = "qabatz.testing"
            implementationClass = "eu.qabatz.gradle.plugins.TestingPlugin"
        }
        create("observability") {
            id = "qabatz.observability"
            implementationClass = "eu.qabatz.gradle.plugins.ObservabilityPlugin"
        }
        create("kafka-consumer") {
            id = "qabatz.kafka-consumer"
            implementationClass = "eu.qabatz.gradle.plugins.KafkaConsumerPlugin"
        }
        create("kafka-producer") {
            id = "qabatz.kafka-producer"
            implementationClass = "eu.qabatz.gradle.plugins.KafkaProducerPlugin"
        }
        create("pipeline-service") {
            id = "qabatz.pipeline-service"
            implementationClass = "eu.qabatz.gradle.plugins.PipelineServicePlugin"
        }
        create("jooq") {
            id = "qabatz.jooq"
            implementationClass = "eu.qabatz.gradle.plugins.JooqPlugin"
        }
        create("secrets") {
            id = "qabatz.secrets"
            implementationClass = "eu.qabatz.gradle.plugins.SecretsPlugin"
        }
    }

    testSourceSets(sourceSets["functionalTest"])
}

tasks.register<Test>("functionalTest") {
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("functionalTest")
}

// Publishing configuration -- version catalog + Forgejo Maven registry
publishing {
    publications {
        // java-gradle-plugin auto-publishes convention plugin artifacts.
        // The version catalog needs a manual publication:
        create<MavenPublication>("versionCatalog") {
            from(components["versionCatalog"])
            artifactId = "qabatz-gradle-plugins-catalog"
        }
    }

    repositories {
        maven {
            name = "Forgejo"
            val repoUrl =
                findProperty("publishUrl")?.toString()
                    ?: "https://git.hestia-ng.eu/api/packages/qabatz/maven"
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
