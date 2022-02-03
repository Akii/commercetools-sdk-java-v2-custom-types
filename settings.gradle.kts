pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
    }
}

rootProject.name = "ctp-product-type-generator"

include(":ctp-product-type")
project(":ctp-product-type").projectDir = file("ctp-product-type")
include("ctp-generator")
