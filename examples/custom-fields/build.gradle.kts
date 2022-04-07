import de.akii.commercetools.api.customtypes.plugin.gradle.commercetoolsCustomTypes

plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("de.akii.commercetools.api.customtypes") version "0.0.22"
}

group "org.example"
version "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("com.commercetools.sdk:commercetools-sdk-java-api:8.2.0")
    implementation("com.commercetools.sdk:commercetools-http-client:8.2.0")
}

application {
    mainClass.set("org.example.MainKt")
}

commercetoolsCustomTypes {
    packageName = "org.example.models"

    customFields {
        typesFile = File("./src/main/resources/types.json")
        isFieldRequired = { _, _ -> true }
    }
}