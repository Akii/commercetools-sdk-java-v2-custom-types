package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.product.Product
import com.fasterxml.jackson.databind.module.SimpleModule
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.CustomProductDeserializerClassName
import de.akii.commercetools.api.customtypes.generator.common.deserializeUsing
import de.akii.commercetools.api.customtypes.generator.product.customProductDeserializer
import de.akii.commercetools.api.customtypes.generator.product.generateProductFile
import de.akii.commercetools.api.customtypes.generator.types.ProductType

data class Configuration(val packageName: String)

fun productFiles(productTypes: List<ProductType>, config: Configuration): List<FileSpec> {
    val productDeserializerFile = FileSpec
        .builder("${config.packageName}.product", "deserializer")
        .addType(customProductDeserializer(productTypes, config))
        .build()

    val apiModuleFile = FileSpec
        .builder(config.packageName, "apiModules")
        .addType(customProductInterface(config))
        .addType(customProductApiModule(config))
        .build()

    return listOf(productDeserializerFile, apiModuleFile) + productTypes.flatMap {
        generateProductFile(it, config)
    }
}

fun customProductInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "CustomProduct"))
        .addAnnotation(deserializeUsing(CustomProductDeserializerClassName(config).className))
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
        })
        .build()
