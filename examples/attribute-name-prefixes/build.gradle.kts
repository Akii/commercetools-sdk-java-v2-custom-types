import de.akii.commercetools.api.customtypes.plugin.gradle.commercetoolsCustomTypes

plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("de.akii.commercetools.api.customtypes") version "0.0.21"
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

    productTypes {
        productTypesFile = File("./src/main/resources/productTypes.json")
        attributeToPropertyName = { productType, attribute ->
            attribute
                .name
                .replace("${productType.key}_", "")
                .split('-', '_')
                .joinToString("") { part ->
                    part.replaceFirstChar { it.toString().toUpperCase() }
                }
                .replaceFirstChar { it.toString().toLowerCase() }
        }
        isAttributeRequired = { _, _ -> true }
    }
}

inline fun String.replaceFirstChar(transform: (Char) -> String): String {
    return if (isNotEmpty()) transform(this[0]) + substring(1) else this
}