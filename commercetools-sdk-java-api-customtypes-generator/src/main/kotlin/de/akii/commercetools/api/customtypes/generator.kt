package de.akii.commercetools.api.customtypes

import com.squareup.kotlinpoet.FileSpec
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.deserializationFiles
import de.akii.commercetools.api.customtypes.generator.model.*
import de.akii.commercetools.api.customtypes.generator.modelFiles

fun generate(config: Configuration): List<FileSpec> {
    val typedResources = typedResources(config)

    return modelFiles(typedResources, config) +
            deserializationFiles(typedResources, config)
}