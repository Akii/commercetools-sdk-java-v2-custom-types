package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product.ProductData
import com.commercetools.api.models.product.ProductVariant
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.core.type.TypeReference
import com.squareup.kotlinpoet.FileSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.customProductVariantAttributesInterface
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ProductKtTest {

    @Test
    fun `generates custom product classes`() {
        val productType = loadProductType("/product-types/testProductType.json")
        val config = Configuration("test.package", listOf(productType))
        val files = generateProductFiles(
            productType,
            config
        )
        val sourceFiles =
            files.map {
                SourceFile.kotlin("${it.name}.kt", it.toString())
            } + SourceFile.kotlin("CustomProduct.kt", customProductInterfaceFile(config).toString())

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        assertThat(result.classLoader.loadClass("test.package.product.test.TestProduct").isAssignableFrom(Product::class.java))
        assertThat(result.classLoader.loadClass("test.package.product.test.TestProductCatalogData").isAssignableFrom(ProductCatalogData::class.java))
        assertThat(result.classLoader.loadClass("test.package.product.test.TestProductData").isAssignableFrom(ProductData::class.java))
        assertThat(result.classLoader.loadClass("test.package.product.test.TestProductVariant").isAssignableFrom(ProductVariant::class.java))

        val variantAttributeClass = result.classLoader.loadClass("test.package.product.test.TestProductVariantAttributes")

        assertThat(variantAttributeClass).hasDeclaredMethods(
            "getABoolean",
            "setABoolean",
            "getAText",
            "setAText",
            "getALocText",
            "setALocText",
            "getAnEnum",
            "setAnEnum",
            "getALocEnum",
            "setALocEnum",
            "getANumber",
            "setANumber",
            "getSomeMoney",
            "setSomeMoney",
            "getADate",
            "setADate",
            "getATime",
            "setATime",
            "getDateTime",
            "setDateTime",
            "getRefSet",
            "setRefSet",
            "getSameForAll",
            "setSameForAll",
            "getNestedSecondType",
            "setNestedSecondType",
        )
    }

    private fun loadProductType(fileName: String) =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource(fileName),
                object : TypeReference<ProductType>() {}
            )

    private fun customProductInterfaceFile(config: Configuration) =
        FileSpec
            .builder("${config.packageName}.product", "CustomProduct")
            .addType(customProductVariantAttributesInterface(config))
            .build()
}