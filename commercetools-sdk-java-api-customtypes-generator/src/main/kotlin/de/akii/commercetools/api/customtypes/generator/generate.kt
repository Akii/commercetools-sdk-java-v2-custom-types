package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.fasterxml.jackson.databind.module.SimpleModule
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.deserialization.customProductDeserializer
import de.akii.commercetools.api.customtypes.generator.deserialization.customProductVariantAttributesDelegatingDeserializer
import de.akii.commercetools.api.customtypes.generator.deserialization.customProductVariantAttributesModifier
import io.vrap.rmf.base.client.utils.Generated

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
        productFiles(it, config)
    }
}

fun customProductInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "CustomProduct"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeUsing(CustomProductDeserializerClassName(config).className))
        .build()

fun fallbackProductInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackProduct"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(ProductImpl::class.asClassName()))
        .build()

fun customProductVariantAttributesInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(CustomProductVariantAttributesClassName(config).className)
        .addAnnotation(Generated::class)
        .build()

fun customProductApiModule(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "CustomProductApiModule"))
        .addAnnotation(Generated::class)
        .superclass(SimpleModule::class)
        .addInitializerBlock(buildCodeBlock {
            add(
                "addDeserializer(%1T::class.java, %2T())\n",
                Product::class.asClassName(),
                CustomProductDeserializerClassName(config).className,
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                Product::class.asClassName(),
                "CustomProduct"
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                ProductImpl::class.asClassName(),
                "FallbackProduct"
            )
            add(
                "setDeserializerModifier(%1T())\n",
                CustomProductVariantAttributesModifierClassName(config).className,
            )
        })
        .build()
