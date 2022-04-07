package de.akii.commercetools.api.customtypes.plugin.gradle

import com.commercetools.api.defaultconfig.ServiceRegion
import com.commercetools.api.models.product_type.AttributeDefinition
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.FieldDefinition
import com.commercetools.api.models.type.Type
import de.akii.commercetools.api.customtypes.generator.common.*
import org.gradle.api.Action
import java.io.File

open class CustomTypesGeneratorPluginExtension {
    private var credentialsConfigured = false
    internal val credentialsExtension: Credentials by lazy {
        credentialsConfigured = true
        Credentials()
    }

    private var productTypesGeneratorConfigured = false
    internal val productTypesGeneratorExtension: CustomProductTypesGeneratorConfiguration by lazy {
        productTypesGeneratorConfigured = true
        CustomProductTypesGeneratorConfiguration()
    }

    private var typesGeneratorConfigured = false
    internal val typesGeneratorExtension: CustomTypesGeneratorConfiguration by lazy {
        typesGeneratorConfigured = true
        CustomTypesGeneratorConfiguration()
    }

    /** Name of the package for all generated classes */
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
    internal fun productTypesGeneratorConfigured(): Boolean = productTypesGeneratorConfigured
    internal fun typesGeneratorConfigured(): Boolean = typesGeneratorConfigured
}

/**
 * Credentials of the commercetools API client.
 * The scopes `view_types` and `view_products` are required.
 */
open class Credentials {
    var clientId: String? = null
    var clientSecret: String? = null
    var serviceRegion: ServiceRegion? = null
    var projectName: String? = null
}

open class CustomProductTypesGeneratorConfiguration {

    /**
     * Path to the product types JSON file.
     * If given, the plugin will not attempt to fetch the types from commercetools.
     */
    var productTypesFile: File? = null

    /**
     * This function defines which key is used for product type identification.
     *
     * If you configure this function you _must_ also configure the product type resolver with the same
     * implementation.
     */
    var productTypeToKey: (productType: ProductType) -> String = ::productTypeToKey

    /**
     * This function defines how class names are computed from commercetools product types.
     *
     * The library assumes the class names are unique for every product type.
     * Anything else will likely result in a compilation error.
     */
    var productTypeToClassName: (productType: ProductType, productClassType: ProductClassType) -> String = ::productTypeToClassName

    /**
     * This function defines how property names are computed from commercetools attribute definitions.
     *
     * The library assumes that camel case property names starting with a lowercase letter are computed.
     * Anything else will likely result in a compilation error.
     */
    var attributeToPropertyName: (productType: ProductType, attribute: AttributeDefinition) -> String = ::attributeToPropertyName

    /**
     * This function defines whether an attribute is required or not.
     * Required attributes are marked as non-nullable.
     *
     * Use this with caution because JSON deserialization will fail when the runtime attribute is null.
     *
     * The default implementation marks all attributes as not required because the commercetools platform cannot guarantee attributes are set.
     */
    var isAttributeRequired: (productType: ProductType, attribute: AttributeDefinition) -> Boolean = ::isAttributeRequired
}

open class CustomTypesGeneratorConfiguration {

    /**
     * Path to the custom field types JSON file.
     * If given, the plugin will not attempt to fetch the types from commercetools.
     */
    var typesFile: File? = null

    /**
     * This function defines which key is used for type identification.
     *
     * If you configure this function you _must_ also configure the type resolver with the same
     * implementation.
     */
    var typeToKey: (type: Type) -> String = ::typeToKey

    /**
     * This function defines how class names are computed from commercetools custom fields.
     *
     * The library assumes the class names are unique for every type.
     * Anything else will likely result in a compilation error.
     */
    var typeToCustomFieldsClassName: (type: Type) -> String = ::typeToCustomFieldsClassName

    /**
     * This function defines how class names are computed from commercetools resource types.
     *
     * The library assumes the class names are unique for every resource type.
     * Anything else will likely result in a compilation error.
     */
    var typeToResourceClassName: (type: Type, referenceTypeName: String) -> String = ::typeToResourceClassName

    /**
     * This function defines how property names are computed from commercetools field definitions.
     *
     * The library assumes that camel case property names starting with a lowercase letter are computed.
     * Anything else will likely result in a compilation error.
     */
    var fieldToPropertyName: (type: Type, fieldDefinition: FieldDefinition) -> String = ::fieldToPropertyName

    /**
     * This function defines whether a field is required or not.
     * Required fields are marked as non-nullable.
     *
     * Use this with caution because JSON deserialization will fail when the runtime field is null.
     *
     * The default implementation marks all fields as not required because the commercetools platform cannot guarantee fields are set.
     */
    var isFieldRequired: (type: Type, fieldDefinition: FieldDefinition) -> Boolean = ::isFieldRequired
}