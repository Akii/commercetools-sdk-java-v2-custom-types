package de.akii.commercetools.api.customtypes.generator

import com.squareup.kotlinpoet.FileSpec
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.deserialization.*
import de.akii.commercetools.api.customtypes.generator.model.*

fun deserializationFiles(typedResourceFiles: List<TypedResources>, config: Configuration): List<FileSpec> {
    val apiModuleFile = apiModulesFile(typedResourceFiles, config)
    val typedProductDeserializerFile = typedProductDeserializerFile(config)
    val typedResourceDeserializerFiles = typedResourceDeserializerFiles(typedResourceFiles, config)
    val typedResourcesDeserializerFile = typedResourcesDeserializerFile(config)

    return listOf(
        apiModuleFile,
        typedProductDeserializerFile,
        typedResourcesDeserializerFile
    ) + typedResourceDeserializerFiles
}

fun apiModulesFile(typedResourceFiles: List<TypedResources>, config: Configuration): FileSpec {
    val file = FileSpec
        .builder(config.packageName, "apiModules")
        .addType(typeResolverInterface(config))

    if (config.productTypes.isNotEmpty()) {
        file
            .addType(productMixInInterface(config))
            .addType(fallbackProductInterface(config))
            .addType(typedProductApiModule(config))
    }

    if (config.customTypes.isNotEmpty()) {
        typedResourceFiles.forEach {
            file.addType(resourceMixInInterface(it, config))
            file.addType(fallbackResourceInterface(it, config))
        }
        file.addType(typedResourcesApiModule(typedResourceFiles, config))
    }

    return file.build()
}

fun typedProductDeserializerFile(config: Configuration): FileSpec =
    FileSpec
        .builder("${config.packageName}.product", "deserializer")
        .addFunction(defaultProductTypeToKey)
        .addType(productTypeResolver(config))
        .addType(typedProductDeserializer(config))
        .addType(typedProductBeanDeserializerModifier(config))
        .addType(typedProductDelegatingDeserializer(config))
        .addType(typedProductVariantAttributesDelegatingDeserializer(config))
        .build()

fun typedResourceDeserializerFiles(typedResources: List<TypedResources>, config: Configuration): List<FileSpec> =
    typedResources
        .map {
            FileSpec
                .builder(it.packageName, "deserializer")
                .addType(typedResourceDeserializer(it, config))
                .build()
        }

fun typedResourcesDeserializerFile(config: Configuration) =
    FileSpec
        .builder("${config.packageName}.typed_resources", "deserializer")
        .addFunction(defaultTypeToKey)
        .addType(typedResourceTypeResolver(config))
        .addType(typedResourceBeanDeserializerModifier(config))
        .addType(typedResourceDelegatingDeserializer(config))
        .build()