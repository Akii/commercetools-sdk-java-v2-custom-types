package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.type.ResourceTypeId
import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.type.TypeReference
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.customFieldsFiles
import de.akii.commercetools.api.customtypes.generator.typedResourceFiles
import de.akii.commercetools.api.customtypes.generator.typedResourcesCommonFile
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class TypedResourceKtTest {

    private val types =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource("/types/types.json"),
                object : TypeReference<List<Type>>() {})

    private val resourceTypeIdsHandled = listOf(
        ResourceTypeId.ADDRESS,
        ResourceTypeId.ASSET,
        ResourceTypeId.BUSINESS_UNIT,
        ResourceTypeId.CART_DISCOUNT,
        ResourceTypeId.CATEGORY,
        ResourceTypeId.CHANNEL,
        ResourceTypeId.CUSTOMER,
        ResourceTypeId.CUSTOMER_GROUP,
        ResourceTypeId.CUSTOM_LINE_ITEM,
        ResourceTypeId.DISCOUNT_CODE,
        ResourceTypeId.INVENTORY_ENTRY,
        ResourceTypeId.LINE_ITEM,
        ResourceTypeId.ORDER,
        ResourceTypeId.ORDER_EDIT,
        ResourceTypeId.ORDER_DELIVERY,
        ResourceTypeId.ORDER_PARCEL,
        ResourceTypeId.ORDER_RETURN_ITEM,
        ResourceTypeId.PAYMENT,
        ResourceTypeId.PRODUCT_PRICE,
        ResourceTypeId.PRODUCT_SELECTION,
        ResourceTypeId.REVIEW,
        ResourceTypeId.SHIPPING_METHOD,
        ResourceTypeId.SHOPPING_LIST,
        ResourceTypeId.STANDALONE_PRICE,
        ResourceTypeId.STORE,
        ResourceTypeId.PAYMENT_INTERFACE_INTERACTION,
        ResourceTypeId.SHOPPING_LIST_TEXT_LINE_ITEM,
        ResourceTypeId.TRANSACTION,
        ResourceTypeId.QUOTE
    )

    @Test
    fun `generates typed resources`() {
        val config = Configuration("test.package", listOf(), types, emptyMap())
        val files = typedResourceFiles(typedResources(config), config) + customFieldsFiles(config) + typedResourcesCommonFile(config)

        val sourceFiles = files.filterNotNull().map {
            SourceFile.kotlin("${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `resource type ids case analysis is exhaustive`() {
        ResourceTypeId
            .values()
            .forEach {
                Assertions.assertThat(resourceTypeIdsHandled).contains(it)
            }
    }

}