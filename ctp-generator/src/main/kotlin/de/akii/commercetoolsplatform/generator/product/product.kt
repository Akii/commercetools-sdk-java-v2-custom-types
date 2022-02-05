package de.akii.commercetoolsplatform.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetoolsplatform.types.common.CreatedBy
import de.akii.commercetoolsplatform.types.common.LastModifiedBy
import de.akii.commercetoolsplatform.types.common.Reference
import de.akii.commercetoolsplatform.types.producttype.ProductType
import kotlinx.serialization.Serializable
import javax.annotation.processing.Generated

fun generateCodeForProduct(
    productType: ProductType,
    productClassName: ClassName,
    productSerializerClassName: ClassName,
    config: Configuration
): FileSpec {
    val productName = productTypeNameToClassName(productType.name)
    val packageName = "${config.packageName}.${productName.lowercase()}"
    val productTypeClassName = ClassName(packageName, productName)

    val (attributeClassName, attributeTypeSpec) = generateCodeForProductVariantAttributes(
        productName,
        packageName,
        productType.attributes
    )
    val (variantClassName, variantTypeSpec) = generateCodeForProductVariant(
        productName,
        packageName,
        attributeClassName
    )
    val (productDataClassName, productDataTypeSpec) = generateCodeForProductData(
        productName,
        packageName,
        variantClassName
    )
    val (masterDataClassName, masterDataTypeSpec) = generateCodeForMasterData(
        productName,
        packageName,
        productDataClassName
    )

    val product = TypeSpec
        .classBuilder(productTypeClassName)
        .superclass(productClassName)
        .addModifiers(KModifier.DATA)
        .addAnnotation(Generated::class)
        .addAnnotation(serializableBy(productSerializerClassName))
        .addProperty("id", String::class)
        .addProperty("version", Int::class)
        .addProperty("lastMessageSequenceNumber", Int::class)
        .addProperty(dateTimeProperty("createdAt"))
        .addProperty("createdBy", CreatedBy::class)
        .addProperty(dateTimeProperty("lastModifiedAt"))
        .addProperty("lastModifiedBy", LastModifiedBy::class)
        .addProperty("productType", Reference::class)
        .addProperty("masterData", masterDataClassName)
        .build()

    return FileSpec
        .builder(packageName, productName)
        .addType(product)
        .addType(masterDataTypeSpec)
        .addType(productDataTypeSpec)
        .addType(variantTypeSpec)
        .addType(attributeTypeSpec)
        .build()
}

fun generateCodeForMasterData(
    productName: String,
    packageName: String,
    productDataClassName: ClassName
): Pair<ClassName, TypeSpec> {
    val className = ClassName(packageName, "${productName}MasterData")
    val type = TypeSpec
        .classBuilder(className)
        .addAnnotation(Generated::class)
        .addAnnotation(serializableBy(Serializable::class))
        .addProperty("current", productDataClassName)
        .addProperty("staged", productDataClassName)
        .addProperty("published", Boolean::class)
        .addProperty("hasStagedChanges", Boolean::class)
        .build()

    return className to type
}

fun generateCodeForProductData(
    productName: String,
    packageName: String,
    variantClassName: ClassName
): Pair<ClassName, TypeSpec> {
    val className = ClassName(packageName, "${productName}ProductData")
    val type = TypeSpec
        .classBuilder(className)
        .addAnnotation(Generated::class)
        .addAnnotation(serializableBy(Serializable::class))
        .addProperty("name", localizedStringType)
        .addProperty("categories", listTypeName(Reference::class))
        .addProperty("categoryOrderHints", mapTypeName(String::class, String::class))
        .addProperty("description", localizedStringType.copy(nullable = true))
        .addProperty("slug", localizedStringType)
        .addProperty("metaTitle", localizedStringType.copy(nullable = true))
        .addProperty("metaDescription", localizedStringType.copy(nullable = true))
        .addProperty("metaKeywords", localizedStringType.copy(nullable = true))
        .addProperty("masterVariant", variantClassName)
        .addProperty("variants", listTypeName(variantClassName))
        .addProperty("searchKeywords", mapTypeName(String::class, Any::class))
        .build()

    return className to type
}