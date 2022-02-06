plugins {
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(project(":ctp-types"))
    implementation(project(":ctp-generator"))
    implementation("com.commercetools.sdk:commercetools-sdk-java-api:7.6.0")
}

gradlePlugin {
    plugins {
        create("ctpGeneratorPlugin") {
            id = "de.akii.commercetools"
            implementationClass = "de.akii.commercetoolsplatform.plugin.gradle.CTPGeneratorGradlePlugin"
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/src")
        }
    }
}