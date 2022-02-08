package de.akii.commercetools.api.customtypes.generator

import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.product.generateProductFile
import de.akii.commercetools.api.customtypes.generator.types.ProductType

data class Configuration(val packageName: String)

fun generateProductTypeFiles(productTypes: List<ProductType>, config: Configuration): List<FileSpec> {
    val productSerializerClassName = ClassName("${config.packageName}.serializer", "ProductSerializer")

    val productSerializer = generateProductSerializer(productSerializerClassName, productTypes, config)
    val file = FileSpec
        .builder(config.packageName, "Product")
        .build()

    return listOf(file, productSerializer) + productTypes.flatMap {
        generateProductFile(it, config)
    }
}

fun generateProductSerializer(
    productSerializerClassName: ClassName,
    productTypes: List<ProductType>,
    config: Configuration
): FileSpec =
    FileSpec.builder(config.packageName, "serializer").build()
