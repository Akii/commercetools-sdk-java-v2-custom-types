description = "Gradle Plugin that generates custom types defined in commercetools projects"

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

val commercetoolsSdkApiVersion: String by project
val kotlinPoetVersion: String by project

dependencies {
    compileOnly(kotlin("gradle-plugin-api"))

    api(project(":commercetools-sdk-java-api-customtypes-generator"))
    compileOnly("com.squareup:kotlinpoet:$kotlinPoetVersion")
    api("com.commercetools.sdk:commercetools-sdk-java-api:$commercetoolsSdkApiVersion")
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
            description = description
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

    sourcesJar {
        dependsOn(generateDefaultVersion)
    }
}