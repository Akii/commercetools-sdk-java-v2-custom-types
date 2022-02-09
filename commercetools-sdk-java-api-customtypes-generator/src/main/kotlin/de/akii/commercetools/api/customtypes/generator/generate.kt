package de.akii.commercetools.api.customtypes.generator

import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.CustomProductClassName
import de.akii.commercetools.api.customtypes.generator.product.customProductDeserializer
import de.akii.commercetools.api.customtypes.generator.product.generateProductFile
import de.akii.commercetools.api.customtypes.generator.types.ProductType

data class Configuration(val packageName: String)

fun productFiles(productTypes: List<ProductType>, config: Configuration): List<FileSpec> {
    val productFile = FileSpec
        .builder("${config.packageName}.product", "product")
        .addType(customProductDeserializer(productTypes, config))
        .build()

    val apiModuleFile = FileSpec
        .builder(config.packageName, "apiModule")
        .addType(apiModule(config))
        .build()

    return listOf(productFile) + productTypes.flatMap {
        generateProductFile(it, config)
    }
}

fun apiModule(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(CustomProductClassName(config).className)
        .build()
