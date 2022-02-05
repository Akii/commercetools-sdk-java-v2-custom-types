package de.akii.commercetoolsplatform.generator

import com.squareup.kotlinpoet.*
import de.akii.commercetoolsplatform.types.producttype.*
import javax.annotation.processing.Generated

data class Configuration(val packageName: String)

fun generateCodeForProductTypes(productTypes: List<ProductType>, config: Configuration): List<FileSpec> {
    val productClassName = ClassName(config.packageName, "Product")
    val productSerializerClassName = ClassName("${config.packageName}.serializer", "ProductSerializer")

    val productClass = TypeSpec.classBuilder(productClassName)
        .addAnnotation(Generated::class)
        .build()
    val productSerializer = generateProductSerializer(productSerializerClassName, productTypes, config)
    val file = FileSpec
        .builder(config.packageName, "Product")
        .addType(productClass)
        .build()

    return listOf(file, productSerializer) + productTypes.map {
        generateCodeForProduct(
            it,
            productClassName,
            productSerializerClassName,
            config
        )
    }
}

fun generateProductSerializer(
    productSerializerClassName: ClassName,
    productTypes: List<ProductType>,
    config: Configuration): FileSpec =
    FileSpec.builder(config.packageName, "serializer").build()

