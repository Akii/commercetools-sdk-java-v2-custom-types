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

project(":ctp-types").projectDir = file("ctp-types")
project(":ctp-generator").projectDir = file("ctp-generator")
