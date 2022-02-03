plugins {
    kotlin("jvm")
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

    dependencies {
        implementation(kotlin("stdlib", kotlinVersion))

        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }
}

