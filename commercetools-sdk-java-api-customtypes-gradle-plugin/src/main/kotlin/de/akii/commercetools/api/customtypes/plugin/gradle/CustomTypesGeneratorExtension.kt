package de.akii.commercetools.api.customtypes.plugin.gradle

import de.akii.commercetools.api.customtypes.generator.common.attributeNameToPropertyName
import de.akii.commercetools.api.customtypes.generator.common.productTypeNameToClassNamePrefix
import de.akii.commercetools.api.customtypes.generator.common.productTypeNameToSubPackageName
import java.io.File

open class CustomTypesGeneratorExtension {
    var clientId: String? = null
    var clientSecret: String? = null
    var serviceRegion: String? = null
    var projectName: String? = null
    var productTypesFile: File? = null
    var packageName: String? = null
    var productTypeNameToSubPackageName: (productTypeName: String) -> String = ::productTypeNameToSubPackageName
    var productTypeNameToClassNamePrefix: (productTypeName: String) -> String = ::productTypeNameToClassNamePrefix
    var attributeNameToPropertyName: (attributeName: String) -> String = ::attributeNameToPropertyName
}