package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.cart.CartReference
import com.commercetools.api.models.cart_discount.CartDiscountReference
import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.channel.ChannelReference
import com.commercetools.api.models.common.Reference
import com.commercetools.api.models.common.ReferenceTypeId
import com.commercetools.api.models.customer.CustomerReference
import com.commercetools.api.models.customer_group.CustomerGroupReference
import com.commercetools.api.models.discount_code.DiscountCodeReference
import com.commercetools.api.models.inventory.InventoryEntryReference
import com.commercetools.api.models.order.OrderReference
import com.commercetools.api.models.order_edit.OrderEditReference
import com.commercetools.api.models.payment.PaymentReference
import com.commercetools.api.models.product.ProductReference
import com.commercetools.api.models.product_discount.ProductDiscountReference
import com.commercetools.api.models.product_selection.ProductSelectionReference
import com.commercetools.api.models.product_type.ProductTypeReference
import com.commercetools.api.models.review.ReviewReference
import com.commercetools.api.models.shipping_method.ShippingMethodReference
import com.commercetools.api.models.shopping_list.ShoppingListReference
import com.commercetools.api.models.state.StateReference
import com.commercetools.api.models.store.StoreReference
import com.commercetools.api.models.tax_category.TaxCategoryReference
import com.commercetools.api.models.type.ResourceTypeId
import com.commercetools.api.models.type.TypeReference
import com.commercetools.api.models.zone.ZoneReference
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asClassName

val jsonCreator =
    AnnotationSpec
        .builder(JsonCreator::class)
        .build()

fun deserializeAs(asClassName: ClassName): AnnotationSpec =
    AnnotationSpec
        .builder(JsonDeserialize::class)
        .addMember(CodeBlock.of("`as` = %T::class", asClassName))
        .build()

fun deserializeUsing(asClassName: ClassName): AnnotationSpec =
    AnnotationSpec
        .builder(JsonDeserialize::class)
        .addMember(CodeBlock.of("using = %T::class", asClassName))
        .build()

fun jsonProperty(name: String): AnnotationSpec =
    AnnotationSpec
        .builder(JsonProperty::class)
        .addMember("%S", name)
        .build()

fun referenceTypeIdToClassName(referenceTypeId: ReferenceTypeId): ClassName =
    when (referenceTypeId) {
        ReferenceTypeId.CART -> CartReference::class.asClassName()
        ReferenceTypeId.CART_DISCOUNT -> CartDiscountReference::class.asClassName()
        ReferenceTypeId.CATEGORY -> CategoryReference::class.asClassName()
        ReferenceTypeId.CHANNEL -> ChannelReference::class.asClassName()
        ReferenceTypeId.CUSTOMER -> CustomerReference::class.asClassName()
        ReferenceTypeId.CUSTOMER_GROUP -> CustomerGroupReference::class.asClassName()
        ReferenceTypeId.DISCOUNT_CODE -> DiscountCodeReference::class.asClassName()
        ReferenceTypeId.INVENTORY_ENTRY -> InventoryEntryReference::class.asClassName()
        ReferenceTypeId.ORDER -> OrderReference::class.asClassName()
        ReferenceTypeId.ORDER_EDIT -> OrderEditReference::class.asClassName()
        ReferenceTypeId.PAYMENT -> PaymentReference::class.asClassName()
        ReferenceTypeId.PRODUCT -> ProductReference::class.asClassName()
        ReferenceTypeId.PRODUCT_DISCOUNT -> ProductDiscountReference::class.asClassName()
        ReferenceTypeId.PRODUCT_SELECTION -> ProductSelectionReference::class.asClassName()
        ReferenceTypeId.PRODUCT_TYPE -> ProductTypeReference::class.asClassName()
        ReferenceTypeId.REVIEW -> ReviewReference::class.asClassName()
        ReferenceTypeId.SHIPPING_METHOD -> ShippingMethodReference::class.asClassName()
        ReferenceTypeId.SHOPPING_LIST -> ShoppingListReference::class.asClassName()
        ReferenceTypeId.STATE -> StateReference::class.asClassName()
        ReferenceTypeId.STORE -> StoreReference::class.asClassName()
        ReferenceTypeId.TAX_CATEGORY -> TaxCategoryReference::class.asClassName()
        ReferenceTypeId.TYPE -> TypeReference::class.asClassName()
        ReferenceTypeId.ZONE -> ZoneReference::class.asClassName()
        else -> Reference::class.asClassName()
    }

fun resourceTypeIdToClassName(resourceTypeId: ResourceTypeId, config: Configuration): ClassName =
    ClassName(
        "${config.packageName}.custom_fields",
        classNamePrefix(resourceTypeId.jsonName) + "CustomFieldsType"
    )

private fun classNamePrefix(name: String): String =
    name
        .split('-')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }