pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

rootProject.name = "ctp-product-type-generator"

include(":ctp-common")
include(":ctp-product-type")
include(":ctp-generator")

project(":ctp-common").projectDir = file("ctp-common")
project(":ctp-product-type").projectDir = file("ctp-product-type")
project(":ctp-generator").projectDir = file("ctp-generator")
