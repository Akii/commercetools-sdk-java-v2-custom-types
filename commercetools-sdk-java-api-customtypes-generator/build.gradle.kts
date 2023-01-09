description = "Code generator for custom types defined in commercetools projects"

dependencies {
    implementation("com.squareup:kotlinpoet:1.12.0")
    api("com.commercetools.sdk:commercetools-sdk-java-api:11.0.0")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    testImplementation("org.assertj:assertj-core:3.24.1")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
    testImplementation("org.reflections:reflections:0.10.2")
}