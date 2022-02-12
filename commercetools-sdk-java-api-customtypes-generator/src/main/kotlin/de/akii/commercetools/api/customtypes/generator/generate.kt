package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.databind.module.SimpleModule
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.product.deserialization.customProductDeserializer
import de.akii.commercetools.api.customtypes.generator.product.deserialization.customProductVariantAttributesDelegatingDeserializer
import de.akii.commercetools.api.customtypes.generator.product.deserialization.customProductVariantAttributesModifier
import de.akii.commercetools.api.customtypes.generator.product.generateProductFile

data class Configuration(
    val packageName: String,
    val productTypes: List<ProductType>)

fun productFiles(config: Configuration): List<FileSpec> {
    val productDeserializerFile = FileSpec
        .builder("${config.packageName}.product", "deserializer")
        .addType(customProductVariantAttributesInterface(config))
        .addType(customProductDeserializer(config))
        .addType(customProductVariantAttributesModifier(config))
        .addType(customProductVariantAttributesDelegatingDeserializer(config))
        .build()

    val apiModuleFile = FileSpec
        .builder(config.packageName, "apiModules")
        .addType(customProductInterface(config))
        .addType(fallbackProductInterface(config))
        .addType(customProductApiModule(config))
        .build()

    return listOf(productDeserializerFile, apiModuleFile) + config.productTypes.flatMap {
        generateProductFile(it, config)
    }
}

fun customProductInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "CustomProduct"))
        .addAnnotation(deserializeUsing(CustomProductDeserializerClassName(config).className))
        .build()

fun fallbackProductInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackProduct"))
        .addAnnotation(deserializeAs(ProductImpl::class.asClassName()))
        .build()

fun customProductVariantAttributesInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(CustomProductVariantAttributesClassName(config).className)
        .build()

fun customProductApiModule(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "CustomProductApiModule"))
        .superclass(SimpleModule::class)
        .addInitializerBlock(buildCodeBlock {
            add(
                "addDeserializer(%1L::class.java, %2L())\n",
                Product::class.asTypeName().canonicalName,
                CustomProductDeserializerClassName(config).className.canonicalName,
            )
            add(
                "setMixInAnnotation(%1L::class.java, %2L::class.java)\n",
                Product::class.asTypeName().canonicalName,
                "CustomProduct"
            )
            add(
                "setMixInAnnotation(%1L::class.java, %2L::class.java)\n",
                ProductImpl::class.asTypeName().canonicalName,
                "FallbackProduct"
            )
            add(
                "setDeserializerModifier(%1L())\n",
                CustomProductVariantAttributesModifierClassName(config).className.canonicalName,
            )
        })
        .build()
