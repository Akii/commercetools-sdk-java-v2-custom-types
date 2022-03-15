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

    internal val productTypesGeneratorExtension = CustomProductTypesGeneratorConfiguration()
    internal val typesGeneratorExtension = CustomTypesGeneratorConfiguration()

    var packageName: String? = null

    fun credentials(action: Action<Credentials>) {
        action.execute(credentialsExtension)
    }

    fun productTypes(action: Action<CustomProductTypesGeneratorConfiguration>) {
        action.execute(productTypesGeneratorExtension)
    }

    fun customFields(action: Action<CustomTypesGeneratorConfiguration>) {
        action.execute(typesGeneratorExtension)
    }

    internal fun credentialsConfigured(): Boolean = credentialsConfigured
}

open class Credentials {
    var clientId: String? = null
    var clientSecret: String? = null
    var serviceRegion: ServiceRegion? = null
    var projectName: String? = null
}

open class CustomProductTypesGeneratorConfiguration {
    var productTypesFile: File? = null
    var productTypeNameToSubPackageName: (productTypeName: String) -> String = ::productTypeNameToSubPackageName
    var productTypeNameToClassNamePrefix: (productTypeName: String) -> String = ::productTypeNameToClassNamePrefix
    var attributeNameToPropertyName: (attributeName: String) -> String = ::attributeNameToPropertyName
}

open class CustomTypesGeneratorConfiguration {
    var typesFile: File? = null
}