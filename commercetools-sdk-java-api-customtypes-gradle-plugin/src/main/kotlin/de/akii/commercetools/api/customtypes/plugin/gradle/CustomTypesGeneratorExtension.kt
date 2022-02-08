package de.akii.commercetools.api.customtypes.plugin.gradle

import java.io.File

open class CustomTypesGeneratorExtension {
    var clientId: String? = null
    var clientSecret: String? = null
    var serviceRegion: String? = null
    var projectName: String? = null
    var productTypesFile: File? = null
    var packageName: String? = null
}