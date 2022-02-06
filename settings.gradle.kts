pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

rootProject.name = "ctp-type-generator"

include(":ctp-types")
include(":ctp-generator")
include(":ctp-generator-gradle-plugin")
include(":ctp-test")

project(":ctp-types").projectDir = file("ctp-types")
project(":ctp-generator").projectDir = file("ctp-generator")
project(":ctp-generator-gradle-plugin").projectDir = file("ctp-generator-gradle-plugin")
project(":ctp-test").projectDir = file("ctp-test")
