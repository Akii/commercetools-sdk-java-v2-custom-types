pluginManagement {
    val kotlinVersion: String by settings
    val dokkaVersion: String by settings
    val pluginPublishPluginVersion: String by settings
    val nexusPublishPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
        id("com.gradle.plugin-publish") version pluginPublishPluginVersion
        id("io.github.gradle-nexus.publish-plugin") version nexusPublishPluginVersion
    }
}

rootProject.name = "commercetools-sdk-java-v2-custom-types"

include(":commercetools-sdk-java-api-customtypes-generator")
include(":commercetools-sdk-java-api-customtypes-gradle-plugin")

project(":commercetools-sdk-java-api-customtypes-generator").projectDir = file("commercetools-sdk-java-api-customtypes-generator")
project(":commercetools-sdk-java-api-customtypes-gradle-plugin").projectDir = file("commercetools-sdk-java-api-customtypes-gradle-plugin")
