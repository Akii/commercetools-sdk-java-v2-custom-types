description = "Gradle Plugin that generates type-safe models for product-types and other custom types defined in commercetools projects"

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

var commercetoolsSdkApiVersion: String? = null

dependencies {
    compileOnly(kotlin("gradle-plugin-api"))

    api(project(":commercetools-sdk-java-api-customtypes-generator"))
    compileOnly("com.squareup:kotlinpoet:1.14.2")
    api("com.commercetools.sdk:commercetools-sdk-java-api:14.5.0") {
        commercetoolsSdkApiVersion = version
    }
}

gradlePlugin {
    plugins {
        register("customTypesGeneratorPlugin") {
            id = "de.akii.commercetools.api.customtypes"
            implementationClass = "de.akii.commercetools.api.customtypes.plugin.gradle.CustomTypesGeneratorGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/Akii/commercetools-sdk-java-v2-custom-types"
    vcsUrl = "https://github.com/Akii/commercetools-sdk-java-v2-custom-types"

    (plugins) {
        "customTypesGeneratorPlugin" {
            displayName = "commercetools API custom types generator Gradle Plugin"
            description = "Gradle Plugin that generates type-safe models for product-types and other custom types defined in commercetools projects"
            tags = listOf("commercetools", "kotlin", "custom-types", "code-generator", "product-types")
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/src")
        }
    }
}

tasks {
    val generateDefaultVersion by registering {
        val fileName = "PluginVersion.kt"
        val defaultVersionFile = File("$buildDir/generated/src/de/akii/commercetools/api/customtypes/plugin/gradle", fileName)

        inputs.property(fileName, project.version)
        outputs.file(defaultVersionFile)

        doFirst {
            defaultVersionFile.parentFile.mkdirs()
            defaultVersionFile.writeText(
                """
                package de.akii.commercetools.api.customtypes.plugin.gradle
                internal const val DEFAULT_PLUGIN_VERSION = "${project.version}"
                internal const val DEFAULT_COMMERCETOOLS_VERSION = "$commercetoolsSdkApiVersion"

                """.trimIndent()
            )
        }
    }
    compileKotlin {
        dependsOn(generateDefaultVersion)
    }
    publishPlugins {
        doFirst {
            if (System.getProperty("gradle.publish.key") == null) {
                System.setProperty("gradle.publish.key", System.getenv("GRADLE_PUBLISH_KEY"))
            }
            if (System.getProperty("gradle.publish.secret") == null) {
                System.setProperty("gradle.publish.secret", System.getenv("GRADLE_PUBLISH_SECRET"))
            }
        }
    }
    publishing {
        publications {
            afterEvaluate {
                named<MavenPublication>("customTypesGeneratorPluginPluginMarkerMaven") {
                    pom {
                        name.set(artifactId)
                        description.set("Plugin descriptor for commercetools custom types generator Gradle plugin")
                    }
                }
            }
        }
    }
}