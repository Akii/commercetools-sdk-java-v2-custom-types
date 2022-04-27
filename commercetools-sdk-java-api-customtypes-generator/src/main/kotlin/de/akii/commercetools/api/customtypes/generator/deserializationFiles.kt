package de.akii.commercetools.api.customtypes.generator

import com.squareup.kotlinpoet.FileSpec
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.deserialization.*
import de.akii.commercetools.api.customtypes.generator.model.*

fun deserializationFiles(typedResourceFiles: List<TypedResources>, config: Configuration): List<FileSpec> {
    val files = mutableListOf<FileSpec>()

    files.add(apiModulesFile(typedResourceFiles, config))

    if (config.productTypes.isNotEmpty()) {
        files.add(typedProductDeserializerFile(config))
    }

    if (config.customTypes.isNotEmpty()) {
        files.addAll(typedResourceDeserializerFiles(typedResourceFiles, config))
        files.add(typedResourcesDeserializerFile(config))
    }

    if (config.customObjectTypes.isNotEmpty()) {
        files.add(typedCustomObjectsDeserializerFile(config))
    }

    return files
}

fun apiModulesFile(typedResourceFiles: List<TypedResources>, config: Configuration): FileSpec {
    val file = FileSpec
        .builder(config.packageName, "apiModules")
        .addType(typeResolverInterface(config))

    if (config.productTypes.isNotEmpty()) {
        file
            .addType(productMixInInterface(config))
            .addType(fallbackProductInterface(config))
            .addType(productProjectionMixInInterface(config))
            .addType(fallbackProjectionProductInterface(config))
            .addType(typedProductApiModule(config))
    }

    if (config.customTypes.isNotEmpty()) {
        typedResourceFiles.forEach {
            file.addType(resourceMixInInterface(it, config))
            file.addType(fallbackResourceInterface(it, config))
        }
        file.addType(typedResourcesApiModule(typedResourceFiles, config))
    }

    if (config.customObjectTypes.isNotEmpty()) {
        file.addType(customObjectMixInInterface(config))
        file.addType(fallbackCustomObjectInterface(config))
        file.addType(typedCustomObjectsApiModule(config))
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
        .addType(typedProductProjectionDeserializer(config))
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
        .builder("${config.packageName}.custom_fields", "deserializer")
        .addFunction(defaultTypeToKey)
        .addType(typedCustomFieldsTypeResolver(config))
        .addType(typedCustomFieldsBeanDeserializerModifier(config))
        .addType(typedCustomFieldsDelegatingDeserializer(config))
        .build()

fun typedCustomObjectsDeserializerFile(config: Configuration) =
    FileSpec
        .builder("${config.packageName}.custom_objects", "deserializer")
        .addType(typedCustomObjectDeserializer(config))
        .addType(typedCustomObjectsBeanDeserializerModifier(config))
        .addType(typedCustomObjectsDelegatingDeserializer(config))
        .build()