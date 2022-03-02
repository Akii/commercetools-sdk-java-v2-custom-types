package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GenerateKtTest {

    private val productType =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource("/product-types/testProductType.json"),
                object : TypeReference<ProductType>() {})

    private val config = Configuration("test.package", listOf(productType))

    private val testProducts = javaClass.getResource("/products/testProducts.json")

    @Test
    fun `api module can deserialize custom product types`() {
        val sourceFiles = productFiles(config).map {
            SourceFile.kotlin("${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        val apiModule = result.classLoader
            .loadClass("test.package.CustomProductApiModule")
            .getDeclaredConstructor()
            .newInstance() as SimpleModule

        val products = JsonUtils
            .createObjectMapper()
            .registerModule(apiModule)
            .readValue(testProducts, object : TypeReference<List<Product>>() {})

        val testProduct = products[0]
        val fallbackProduct = products[1]

        assertThat(result.classLoader.loadClass("test.package.product.test.TestProduct").isInstance(testProduct))
        assertThat(ProductImpl::class.java.isInstance(fallbackProduct))
    }

}