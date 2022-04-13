description = "Code generator for custom types defined in commercetools projects"

dependencies {
    implementation("com.squareup:kotlinpoet:1.11.0")
    api("com.commercetools.sdk:commercetools-sdk-java-api:8.5.1")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.8")
    testImplementation("org.assertj:assertj-core:3.22.0")
}