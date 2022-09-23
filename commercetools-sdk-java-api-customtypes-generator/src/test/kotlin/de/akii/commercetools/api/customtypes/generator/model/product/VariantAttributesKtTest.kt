package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product_type.AttributeReferenceTypeId
import com.commercetools.api.models.product_type.AttributeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.reflections.Reflections

internal class VariantAttributesKtTest {

    private val attributeTypesHandled = listOf(
        "AttributeBooleanType",
        "AttributeTextType",
        "AttributeLocalizableTextType",
        "AttributeEnumType",
        "AttributeLocalizedEnumType",
        "AttributeNumberType",
        "AttributeMoneyType",
        "AttributeDateType",
        "AttributeTimeType",
        "AttributeDateTimeType",
        "AttributeReferenceType",
        "AttributeSetType",
        "AttributeNestedType"
    )

    private val attributeReferenceTypeIdsHandled = listOf(
        AttributeReferenceTypeId.CART,
        AttributeReferenceTypeId.CATEGORY,
        AttributeReferenceTypeId.CHANNEL,
        AttributeReferenceTypeId.CUSTOMER,
        AttributeReferenceTypeId.KEY_VALUE_DOCUMENT,
        AttributeReferenceTypeId.ORDER,
        AttributeReferenceTypeId.PRODUCT,
        AttributeReferenceTypeId.PRODUCT_TYPE,
        AttributeReferenceTypeId.REVIEW,
        AttributeReferenceTypeId.SHIPPING_METHOD,
        AttributeReferenceTypeId.STATE,
        AttributeReferenceTypeId.ZONE
    )

    @Test
    fun `attribute types case analysis is exhaustive`() {
        Reflections("com.commercetools.api.models.product_type")
            .getSubTypesOf(AttributeType::class.java)
            .filter { it.isInterface }
            .forEach {
                assertThat(attributeTypesHandled).contains(it.simpleName)
            }
    }

    @Test
    fun `attribute reference type ids case analysis is exhaustive`() {
        AttributeReferenceTypeId
            .values()
            .forEach {
                assertThat(attributeReferenceTypeIdsHandled).contains(it)
            }
    }

}