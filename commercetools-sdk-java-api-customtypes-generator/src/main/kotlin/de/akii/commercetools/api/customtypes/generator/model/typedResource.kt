package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.cart.*
import com.commercetools.api.models.cart_discount.CartDiscount
import com.commercetools.api.models.cart_discount.CartDiscountImpl
import com.commercetools.api.models.category.Category
import com.commercetools.api.models.category.CategoryImpl
import com.commercetools.api.models.channel.Channel
import com.commercetools.api.models.channel.ChannelImpl
import com.commercetools.api.models.common.*
import com.commercetools.api.models.customer.Customer
import com.commercetools.api.models.customer.CustomerImpl
import com.commercetools.api.models.customer_group.CustomerGroup
import com.commercetools.api.models.customer_group.CustomerGroupImpl
import com.commercetools.api.models.discount_code.DiscountCode
import com.commercetools.api.models.discount_code.DiscountCodeImpl
import com.commercetools.api.models.inventory.InventoryEntry
import com.commercetools.api.models.inventory.InventoryEntryImpl
import com.commercetools.api.models.order.*
import com.commercetools.api.models.order_edit.OrderEdit
import com.commercetools.api.models.order_edit.OrderEditImpl
import com.commercetools.api.models.payment.*
import com.commercetools.api.models.review.Review
import com.commercetools.api.models.review.ReviewImpl
import com.commercetools.api.models.shipping_method.ShippingMethod
import com.commercetools.api.models.shipping_method.ShippingMethodImpl
import com.commercetools.api.models.shopping_list.ShoppingList
import com.commercetools.api.models.shopping_list.ShoppingListImpl
import com.commercetools.api.models.shopping_list.TextLineItem
import com.commercetools.api.models.shopping_list.TextLineItemImpl
import com.commercetools.api.models.store.Store
import com.commercetools.api.models.store.StoreImpl
import com.commercetools.api.models.type.ResourceTypeId
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated
import kotlin.reflect.KClass

val resourceTypeNameToSubPackage: (String) -> String = { it.split('-').joinToString("_") }
val resourceTypeNameToClassName: (name: String) -> String = { name ->
    "Typed" + name.split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
}

data class TypedResourceFile(
    val resourceInterface: KClass<*>,
    val resourceDefaultImplementation: KClass<*>,
    val typedResourceClassName: ClassName,
    val file: FileSpec
)

fun typedResourceFiles(config: Configuration): List<TypedResourceFile> =
    config.customTypes
        .flatMap { it.resourceTypeIds }
        .toSet()
        .flatMap { typedResourceFiles(it, config) }

private fun typedResourceFiles(resourceTypeId: ResourceTypeId, config: Configuration): List<TypedResourceFile> =
    when (resourceTypeId) {
        ResourceTypeId.ORDER -> listOf(
            typedResourceFile(resourceTypeId, "order", OrderImpl::class, Order::class, config),
            typedResourceFile(resourceTypeId, "cart", CartImpl::class, Cart::class, config),
            typedResourceFile(resourceTypeId, "return-item", ReturnItemImpl::class, ReturnItem::class, config),
        )
        else -> listOf(
            typedResourceFile(
                resourceTypeId,
                resourceTypeId.jsonName,
                resourceTypeIdToClasses(resourceTypeId).first,
                resourceTypeIdToClasses(resourceTypeId).second,
                config
            )
        )
    }

private fun typedResourceFile(
    resourceTypeId: ResourceTypeId,
    resourceTypeName: String,
    resourceTypeDefaultImplementation: KClass<*>,
    resourceInterface: KClass<*>,
    config: Configuration
): TypedResourceFile {
    val packageName = "${config.packageName}.${resourceTypeNameToSubPackage(resourceTypeName)}"
    val resourceClassName = resourceTypeNameToClassName(resourceTypeName)
    val customFieldType = resourceTypeIdToClassName(resourceTypeId, config)
    val className = ClassName(packageName, resourceClassName)

    val file = FileSpec
        .builder(packageName, resourceClassName)
        .addType(
            TypeSpec
                .classBuilder(className)
                .addAnnotation(Generated::class)
                .addAnnotation(deserializeAs(className))
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addAnnotation(jsonCreator)
                        .addParameter(
                            ParameterSpec
                                .builder("delegate", resourceTypeDefaultImplementation)
                                .addAnnotation(jsonProperty("delegate"))
                                .build()
                        )
                        .addParameter(
                            ParameterSpec
                                .builder("custom", customFieldType.copy(nullable = true))
                                .addAnnotation(jsonProperty("custom"))
                                .build()
                        )
                        .build()
                )
                .addSuperinterface(resourceInterface, "delegate")
                .addProperty(
                    PropertySpec
                        .builder("custom", customFieldType.copy(nullable = true))
                        .initializer("custom")
                        .addModifiers(KModifier.PRIVATE)
                        .build()
                )
                .addFunction(
                    FunSpec
                        .builder("getCustom")
                        .returns(customFieldType.copy(nullable = true))
                        .addStatement("return this.custom")
                        .addModifiers(KModifier.OVERRIDE)
                        .build()
                )
                .build()
        )
        .build()

    return TypedResourceFile(
        resourceInterface,
        resourceTypeDefaultImplementation,
        className,
        file
    )
}

private fun resourceTypeIdToClasses(resourceTypeId: ResourceTypeId) =
    when (resourceTypeId) {
        ResourceTypeId.ADDRESS -> (AddressImpl::class to Address::class)
        ResourceTypeId.ASSET -> (AssetImpl::class to Asset::class)
        ResourceTypeId.CART_DISCOUNT -> (CartDiscountImpl::class to CartDiscount::class)
        ResourceTypeId.CATEGORY -> (CategoryImpl::class to Category::class)
        ResourceTypeId.CHANNEL -> (ChannelImpl::class to Channel::class)
        ResourceTypeId.CUSTOMER -> (CustomerImpl::class to Customer::class)
        ResourceTypeId.CUSTOMER_GROUP -> (CustomerGroupImpl::class to CustomerGroup::class)
        ResourceTypeId.CUSTOM_LINE_ITEM -> (CustomLineItemImpl::class to CustomLineItem::class)
        ResourceTypeId.DISCOUNT_CODE -> (DiscountCodeImpl::class to DiscountCode::class)
        ResourceTypeId.INVENTORY_ENTRY -> (InventoryEntryImpl::class to InventoryEntry::class)
        ResourceTypeId.LINE_ITEM -> (LineItemImpl::class to LineItem::class)
        ResourceTypeId.ORDER -> (OrderImpl::class to Order::class)
        ResourceTypeId.ORDER_EDIT -> (OrderEditImpl::class to OrderEdit::class)
        ResourceTypeId.ORDER_DELIVERY -> (DeliveryImpl::class to Delivery::class)
        ResourceTypeId.PAYMENT -> (PaymentImpl::class to Payment::class)
        ResourceTypeId.PRODUCT_PRICE -> (PriceImpl::class to Price::class)
        ResourceTypeId.REVIEW -> (ReviewImpl::class to Review::class)
        ResourceTypeId.SHIPPING_METHOD -> (ShippingMethodImpl::class to ShippingMethod::class)
        ResourceTypeId.SHOPPING_LIST -> (ShoppingListImpl::class to ShoppingList::class)
        ResourceTypeId.STORE -> (StoreImpl::class to Store::class)
        ResourceTypeId.PAYMENT_INTERFACE_INTERACTION -> (PaymentAddInterfaceInteractionActionImpl::class to PaymentAddInterfaceInteractionAction::class)
        ResourceTypeId.SHOPPING_LIST_TEXT_LINE_ITEM -> (TextLineItemImpl::class to TextLineItem::class)
        ResourceTypeId.TRANSACTION -> (TransactionImpl::class to Transaction::class)
        else -> error("Unknown resource type id ${resourceTypeId.jsonName}")
    }