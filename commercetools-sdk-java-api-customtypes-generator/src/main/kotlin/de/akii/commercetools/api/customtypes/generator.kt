package de.akii.commercetools.api.customtypes

import com.squareup.kotlinpoet.FileSpec
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.customFieldsFile
import de.akii.commercetools.api.customtypes.generator.deserialization.*
import de.akii.commercetools.api.customtypes.generator.productFiles
import de.akii.commercetools.api.customtypes.generator.typedResourceFiles

fun generate(config: Configuration): List<FileSpec> {
    val customFieldsFile = customFieldsFile(config.customTypes, config)
    val typedResourceFiles = typedResourceFiles(config)
    val customFieldsDeserializerFile = customFieldsDeserializerFile(typedResourceFiles, config)
    val customProductFiles = config.productTypes.flatMap { productFiles(it, config) }
    val apiModuleFile = apiModulesFile(typedResourceFiles, config)

    return listOf(
        customFieldsFile,
        customFieldsDeserializerFile,
        apiModuleFile
    ) + typedResourceFiles.map { it.file } + customProductFiles
}