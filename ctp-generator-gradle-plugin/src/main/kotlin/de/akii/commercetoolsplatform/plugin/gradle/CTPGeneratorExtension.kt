package de.akii.commercetoolsplatform.plugin.gradle

import com.commercetools.api.defaultconfig.ServiceRegion
import java.io.File

open class CTPGeneratorExtension {
    var clientId: String? = null
    var clientSecret: String? = null
    var serviceRegion: ServiceRegion? = null
    var projectName: String? = null
    var productTypesFile: File? = null
}