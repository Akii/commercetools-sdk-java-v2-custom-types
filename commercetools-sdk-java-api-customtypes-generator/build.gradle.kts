description = "Code generator for custom types defined in commercetools projects"

val kotlinPoetVersion: String by project
val commercetoolsSdkApiVersion: String by project
val kotlinCompileTestingVersion: String by project

dependencies {
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    api("com.commercetools.sdk:commercetools-sdk-java-api:$commercetoolsSdkApiVersion")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:$kotlinCompileTestingVersion")
    testImplementation("org.assertj:assertj-core:3.22.0")
}