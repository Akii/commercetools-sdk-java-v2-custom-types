package de.akii.commercetools.api.customtypes

import com.squareup.kotlinpoet.FileSpec
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.model.customFieldsFile
import de.akii.commercetools.api.customtypes.generator.deserialization.*
import de.akii.commercetools.api.customtypes.generator.model.typedResourceFiles
import de.akii.commercetools.api.customtypes.generator.productFiles

fun generate(config: Configuration): List<FileSpec> {
    val customFieldsFile = customFieldsFile(config)
    val typedResourceFiles = typedResourceFiles(config)
    val customFieldsDeserializerFile = customFieldsDeserializerFile(config)
    val customProductFiles = productFiles(config)
    val apiModuleFile = apiModulesFile(typedResourceFiles, config)

    return listOf(
        productDeserializerFile(config),
        customFieldsFile,
        customFieldsDeserializerFile,
        apiModuleFile
    ) + typedResourceFiles.map { it.file } + customProductFiles
}