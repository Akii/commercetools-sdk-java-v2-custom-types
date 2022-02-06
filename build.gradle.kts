plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

allprojects {
    buildscript {
        repositories {
            mavenCentral()
            mavenLocal()
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    val kotlinVersion: String by project

    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")

    dependencies {
        implementation(kotlin("stdlib", kotlinVersion))

        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }
}

