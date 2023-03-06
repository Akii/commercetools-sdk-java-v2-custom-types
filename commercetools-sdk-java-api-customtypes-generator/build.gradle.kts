description = "Code generator for custom types defined in commercetools projects"

dependencies {
    implementation("com.squareup:kotlinpoet:1.12.0")
    api("com.commercetools.sdk:commercetools-sdk-java-api:12.1.0")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    testImplementation("org.reflections:reflections:0.10.2")
}