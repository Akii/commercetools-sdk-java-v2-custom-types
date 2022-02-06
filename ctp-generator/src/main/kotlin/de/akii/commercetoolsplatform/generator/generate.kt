package de.akii.commercetoolsplatform.generator

import com.squareup.kotlinpoet.*
import de.akii.commercetoolsplatform.generator.product.generateProductFile
import de.akii.commercetoolsplatform.types.producttype.*

data class Configuration(val packageName: String)

fun generateFilesProductTypes(productTypes: List<ProductType>, config: Configuration): List<FileSpec> {
    val productSerializerClassName = ClassName("${config.packageName}.serializer", "ProductSerializer")

    val productSerializer = generateProductSerializer(productSerializerClassName, productTypes, config)
    val file = FileSpec
        .builder(config.packageName, "Product")
        .build()

    return listOf(file, productSerializer) + productTypes.map {
        generateProductFile(it, config)
    }
}

fun generateProductSerializer(
    productSerializerClassName: ClassName,
    productTypes: List<ProductType>,
    config: Configuration
): FileSpec =
    FileSpec.builder(config.packageName, "serializer").build()
