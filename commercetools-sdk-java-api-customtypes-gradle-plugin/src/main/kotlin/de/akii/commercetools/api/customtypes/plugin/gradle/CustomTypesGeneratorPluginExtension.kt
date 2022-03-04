package de.akii.commercetools.api.customtypes.plugin.gradle

import com.commercetools.api.defaultconfig.ServiceRegion
import de.akii.commercetools.api.customtypes.generator.common.attributeNameToPropertyName
import de.akii.commercetools.api.customtypes.generator.common.productTypeNameToClassNamePrefix
import de.akii.commercetools.api.customtypes.generator.common.productTypeNameToSubPackageName
import org.gradle.api.Action
import java.io.File

open class CustomTypesGeneratorPluginExtension {
    private var credentialsConfigured = false
    internal val credentialsExtension: Credentials by lazy {
        credentialsConfigured = true
        Credentials()
    }

    private var customProductTypesGeneratorConfigured: Boolean = false
    internal val productTypesGeneratorExtension: CustomProductTypesGeneratorConfiguration by lazy {
        customProductTypesGeneratorConfigured = true
        CustomProductTypesGeneratorConfiguration()
    }

    fun credentials(action: Action<Credentials>) {
        action.execute(credentialsExtension)
    }

    fun productTypes(action: Action<CustomProductTypesGeneratorConfiguration>) {
        action.execute(productTypesGeneratorExtension)
    }

    internal fun credentialsConfigured(): Boolean = credentialsConfigured

    internal fun customProductTypesGenerationConfigured(): Boolean = customProductTypesGeneratorConfigured
}

open class Credentials {
    var clientId: String? = null
    var clientSecret: String? = null
    var serviceRegion: ServiceRegion? = null
    var projectName: String? = null
}

open class CustomProductTypesGeneratorConfiguration {
    var packageName: String? = null
    var productTypesFile: File? = null
    var productTypeNameToSubPackageName: (productTypeName: String) -> String = ::productTypeNameToSubPackageName
    var productTypeNameToClassNamePrefix: (productTypeName: String) -> String = ::productTypeNameToClassNamePrefix
    var attributeNameToPropertyName: (attributeName: String) -> String = ::attributeNameToPropertyName
}