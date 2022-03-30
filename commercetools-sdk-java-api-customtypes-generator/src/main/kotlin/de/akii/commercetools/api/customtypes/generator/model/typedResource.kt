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
import com.commercetools.api.models.type.Type
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated
import kotlin.reflect.KClass

data class TypedResources(
    val resourceInterface: KClass<*>,
    val resourceDefaultImplementation: KClass<*>,
    val packageName: String,
    val resources: List<TypedResource>
)

data class TypedResource(
    val type: Type,
    val typedResourceClassName: ClassName,
    val typedResourceSpec: TypeSpec
)

fun typedResourceInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(TypedResourceInterface(config).className)
        .addAnnotation(generated)
        .build()

fun typedResources(config: Configuration): List<TypedResources> =
    config.customTypes
        .flatMap { it.resourceTypeIds }
        .toSet()
        .flatMap { resourceTypeId ->
            typedResources(
                resourceTypeId,
                config.customTypes.filter { it.resourceTypeIds.contains(resourceTypeId) },
                config
            )
        }

private fun typedResources(
    resourceTypeId: ResourceTypeId,
    types: List<Type>,
    config: Configuration
): List<TypedResources> =
    when (resourceTypeId) {
        ResourceTypeId.ORDER -> listOf(
            typedResources("order", types, OrderImpl::class, Order::class, config),
            typedResources("cart", types, CartImpl::class, Cart::class, config),
            typedResources("return-item", types, ReturnItemImpl::class, ReturnItem::class, config),
        )
        else -> listOf(
            typedResources(
                resourceTypeId.jsonName,
                types,
                resourceTypeIdToClasses(resourceTypeId).first,
                resourceTypeIdToClasses(resourceTypeId).second,
                config
            )
        )
    }

private fun typedResources(
    resourceTypeName: String,
    types: List<Type>,
    resourceTypeDefaultImplementation: KClass<*>,
    resourceInterface: KClass<*>,
    config: Configuration
): TypedResources =
    TypedResources(
        resourceInterface,
        resourceTypeDefaultImplementation,
        "${config.packageName}.${resourceTypeNameToSubPackage(resourceTypeName)}",
        types.map {
            typedResource(
                resourceTypeName,
                it,
                resourceTypeDefaultImplementation,
                resourceInterface,
                config
            )
        }
    )

private fun typedResource(
    resourceTypeName: String,
    type: Type,
    resourceTypeDefaultImplementation: KClass<*>,
    resourceInterface: KClass<*>,
    config: Configuration
): TypedResource {
    val customFieldType = TypedCustomFields(type, config).className
    val className = TypedResource(type, resourceTypeName, config).className

    val resourceType = TypeSpec
        .classBuilder(className)
        .addAnnotation(generated)
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
                        .builder("custom", customFieldType)
                        .addAnnotation(jsonProperty("custom"))
                        .build()
                )
                .build()
        )
        .addSuperinterface(resourceInterface, "delegate")
        .addSuperinterface(TypedResourceInterface(config).className)
        .addProperty(
            PropertySpec
                .builder("custom", customFieldType)
                .initializer("custom")
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        .addFunction(
            FunSpec
                .builder("getCustom")
                .returns(customFieldType)
                .addStatement("return this.custom")
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .build()

    return TypedResource(
        type,
        className,
        resourceType
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