package de.akii.commercetools.api.customtypes

import com.squareup.kotlinpoet.FileSpec
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.model.customFieldsFile
import de.akii.commercetools.api.customtypes.generator.deserialization.*
import de.akii.commercetools.api.customtypes.generator.model.productFiles
import de.akii.commercetools.api.customtypes.generator.model.typedResourceFiles
import de.akii.commercetools.api.customtypes.generator.model.typedResources

fun generate(config: Configuration): List<FileSpec> {
    val customFieldsFile = customFieldsFile(config)
    val customTypeResolver = customTypeResolverFile(config)
    val typedResources = typedResources(config)
    val typedResourceFiles = typedResourceFiles(typedResources)
    val typedResourceDeserializers = typedResourceDeserializerFile(typedResources, config)
    val customProductFiles = productFiles(config)
    val apiModuleFile = apiModulesFile(typedResources, config)

    return listOf(
        productDeserializerFile(config), customFieldsFile, apiModuleFile, customTypeResolver
    ) + typedResourceDeserializers + typedResourceFiles + customProductFiles

}