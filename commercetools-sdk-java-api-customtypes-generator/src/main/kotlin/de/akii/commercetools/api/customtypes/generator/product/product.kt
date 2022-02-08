package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.common.CreatedBy
import com.commercetools.api.models.common.LastModifiedBy
import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product_type.ProductTypeReference
import com.commercetools.api.models.product_type.ProductTypeReferenceImpl
import com.commercetools.api.models.review.ReviewRatingStatistics
import com.commercetools.api.models.state.StateReference
import com.commercetools.api.models.tax_category.TaxCategoryReference
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.types.ProductType
import java.time.ZonedDateTime
import io.vrap.rmf.base.client.utils.Generated

fun generateProductFile(
    productType: ProductType,
    config: Configuration
): FileSpec {
    val productClassName = ProductClassName(productType.name, config)
    val productCatalogDataClassName = ProductCatalogDataClassName(productType.name, config)
    val productDataClassName = ProductDataClassName(productType.name, config)
    val productVariantClassName = ProductVariantClassName(productType.name, config)
    val productVariantAttributesClassName = ProductVariantAttributesClassName(productType.name, config)

    val attributeTypeSpec = generateProductVariantAttributes(
        productVariantAttributesClassName,
        productType.attributes,
        config
    )

    val variantTypeSpec = generateProductVariant(
        ProductVariantClassName(productType.name, config),
        productVariantAttributesClassName
    )

    val productDataTypeSpec = generateProductData(
        productDataClassName,
        productVariantClassName
    )

    val masterDataTypeSpec = generateProductCatalogData(
        productCatalogDataClassName,
        productDataClassName
    )

    val product = TypeSpec
        .classBuilder(productClassName.className)
        .addSuperinterface(Product::class)
        .addAnnotation(Generated::class)
        .addCTProperty("id", String::class, initializer = "\"\"")
        .addCTProperty("key", String::class, true)
        .addCTProperty("version", Long::class, initializer = "0")
        .addCTProperty("createdAt", ZonedDateTime::class, initializer = ZONED_DATE_TIME_INITIALIZER)
        .addCTProperty("createdBy", CreatedBy::class, true)
        .addCTProperty("lastModifiedAt", ZonedDateTime::class, initializer = ZONED_DATE_TIME_INITIALIZER)
        .addCTProperty("lastModifiedBy", LastModifiedBy::class, true)
        .addCTProperty("productType", ProductTypeReference::class, initializer = initializerFor(ProductTypeReferenceImpl::class))
        .addCTProperty("masterData", productCatalogDataClassName.className, castedFrom = ProductCatalogData::class, initializer = initializerFor(productCatalogDataClassName.className))
        .addCTProperty("taxCategory", TaxCategoryReference::class, true)
        .addCTProperty("state", StateReference::class, true)
        .addCTProperty("reviewRatingStatistics", ReviewRatingStatistics::class,true)
        .build()

    return FileSpec
        .builder(
            productClassName.className.packageName,
            productClassName.className.simpleName
        )
        .addType(product)
        .addType(masterDataTypeSpec)
        .addType(productDataTypeSpec)
        .addType(variantTypeSpec)
        .addType(attributeTypeSpec)
        .build()
}