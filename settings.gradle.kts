pluginManagement {
    val kotlinVersion: String by settings
    val pluginPublishPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.gradle.plugin-publish") version pluginPublishPluginVersion
    }
}

rootProject.name = "commercetools-sdk-java-v2-custom-types"

include(":commercetools-sdk-java-api-customtypes-generator")
include(":commercetools-sdk-java-api-customtypes-gradle-plugin")

project(":commercetools-sdk-java-api-customtypes-generator").projectDir = file("commercetools-sdk-java-api-customtypes-generator")
project(":commercetools-sdk-java-api-customtypes-gradle-plugin").projectDir = file("commercetools-sdk-java-api-customtypes-gradle-plugin")
