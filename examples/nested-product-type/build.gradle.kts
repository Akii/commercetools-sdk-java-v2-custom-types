import de.akii.commercetools.api.customtypes.plugin.gradle.commercetoolsCustomTypes

plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("de.akii.commercetools.api.customtypes") version "0.0.44"
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

    implementation("com.commercetools.sdk:commercetools-sdk-java-api:9.3.0")
    implementation("com.commercetools.sdk:commercetools-http-client:9.3.0")
}

application {
    mainClass.set("org.example.MainKt")
}

commercetoolsCustomTypes {
    packageName = "org.example.models"

    productTypes {
        productTypesFile = File("./src/main/resources/productTypes.json")
        isAttributeRequired = { _, _ -> true }
    }
}