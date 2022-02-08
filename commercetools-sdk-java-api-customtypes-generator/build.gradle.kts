description = "Code generator for custom types defined in commercetools projects"

val kotlinPoetVersion: String by project
val kotlinxSerializationJsonVersion: String by project
val commercetoolsSdkApiVersion: String by project

plugins {
    kotlin("plugin.serialization")
}

dependencies {
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
    api("com.commercetools.sdk:commercetools-sdk-java-api:$commercetoolsSdkApiVersion")

    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
}