package de.akii.commercetoolsplatform.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetoolsplatform.producttype.ProductType

data class Configuration(val packageName: String)

fun generateCodeForProductTypes(productTypes: List<ProductType>, config: Configuration): List<TypeSpec> {
    val sealedProductClass = TypeSpec.classBuilder("Product")
        .addModifiers(KModifier.SEALED)
        .build()

    return listOf(sealedProductClass) + productTypes.map { generateCodeForProductType(it, config) }
}

fun generateCodeForProductType(productType: ProductType, config: Configuration): TypeSpec {
    return TypeSpec
        .classBuilder(productTypeNameToClassName(productType.name))
        .build()
}

fun productTypeNameToClassName(productTypeName: String): String {
    return productTypeName
}

fun attributeNameToPropertyName(attributeName: String): String {
    return attributeName
}