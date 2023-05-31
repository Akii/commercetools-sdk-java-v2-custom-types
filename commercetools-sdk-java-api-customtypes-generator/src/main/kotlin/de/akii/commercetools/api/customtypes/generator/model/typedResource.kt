package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.associate_role.AssociateRole
import com.commercetools.api.models.associate_role.AssociateRoleBuilder
import com.commercetools.api.models.associate_role.AssociateRoleImpl
import com.commercetools.api.models.business_unit.BusinessUnit
import com.commercetools.api.models.business_unit.BusinessUnitBuilder
import com.commercetools.api.models.business_unit.BusinessUnitImpl
import com.commercetools.api.models.cart.*
import com.commercetools.api.models.cart_discount.CartDiscount
import com.commercetools.api.models.cart_discount.CartDiscountBuilder
import com.commercetools.api.models.cart_discount.CartDiscountImpl
import com.commercetools.api.models.category.Category
import com.commercetools.api.models.category.CategoryBuilder
import com.commercetools.api.models.category.CategoryImpl
import com.commercetools.api.models.channel.Channel
import com.commercetools.api.models.channel.ChannelBuilder
import com.commercetools.api.models.channel.ChannelImpl
import com.commercetools.api.models.common.*
import com.commercetools.api.models.customer.Customer
import com.commercetools.api.models.customer.CustomerBuilder
import com.commercetools.api.models.customer.CustomerImpl
import com.commercetools.api.models.customer_group.CustomerGroup
import com.commercetools.api.models.customer_group.CustomerGroupBuilder
import com.commercetools.api.models.customer_group.CustomerGroupImpl
import com.commercetools.api.models.discount_code.DiscountCode
import com.commercetools.api.models.discount_code.DiscountCodeBuilder
import com.commercetools.api.models.discount_code.DiscountCodeImpl
import com.commercetools.api.models.inventory.InventoryEntry
import com.commercetools.api.models.inventory.InventoryEntryBuilder
import com.commercetools.api.models.inventory.InventoryEntryImpl
import com.commercetools.api.models.order.*
import com.commercetools.api.models.order_edit.OrderEdit
import com.commercetools.api.models.order_edit.OrderEditBuilder
import com.commercetools.api.models.order_edit.OrderEditImpl
import com.commercetools.api.models.payment.*
import com.commercetools.api.models.product_selection.ProductSelection
import com.commercetools.api.models.product_selection.ProductSelectionBuilder
import com.commercetools.api.models.product_selection.ProductSelectionImpl
import com.commercetools.api.models.quote.Quote
import com.commercetools.api.models.quote.QuoteBuilder
import com.commercetools.api.models.quote.QuoteImpl
import com.commercetools.api.models.review.Review
import com.commercetools.api.models.review.ReviewBuilder
import com.commercetools.api.models.review.ReviewImpl
import com.commercetools.api.models.shipping_method.ShippingMethod
import com.commercetools.api.models.shipping_method.ShippingMethodBuilder
import com.commercetools.api.models.shipping_method.ShippingMethodImpl
import com.commercetools.api.models.shopping_list.ShoppingList
import com.commercetools.api.models.shopping_list.ShoppingListBuilder
import com.commercetools.api.models.shopping_list.ShoppingListImpl
import com.commercetools.api.models.shopping_list.TextLineItem
import com.commercetools.api.models.shopping_list.TextLineItemBuilder
import com.commercetools.api.models.shopping_list.TextLineItemImpl
import com.commercetools.api.models.standalone_price.StandalonePrice
import com.commercetools.api.models.standalone_price.StandalonePriceBuilder
import com.commercetools.api.models.standalone_price.StandalonePriceImpl
import com.commercetools.api.models.store.Store
import com.commercetools.api.models.store.StoreBuilder
import com.commercetools.api.models.store.StoreImpl
import com.commercetools.api.models.type.CustomFields
import com.commercetools.api.models.type.ResourceTypeId
import com.commercetools.api.models.type.Type
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import kotlin.reflect.KClass

data class TypedResources(
    val resourceInterface: KClass<*>,
    val resourceDefaultImplementation: KClass<*>,
    val builder: KClass<*>,
    val packageName: String,
    val resources: List<TypedResource>
)

data class TypedResource(
    val type: Type,
    val typedResourceClassName: ClassName,
    val typedResourceSpec: TypeSpec
)

val resourceTypeIdClasses =
    mapOf(
        ResourceTypeId.ADDRESS to Triple(AddressImpl::class, Address::class, AddressBuilder::class),
        ResourceTypeId.ASSET to Triple(AssetImpl::class, Asset::class, AssetBuilder::class),
        ResourceTypeId.ASSOCIATE_ROLE to Triple(AssociateRoleImpl::class, AssociateRole::class, AssociateRoleBuilder::class),
        ResourceTypeId.BUSINESS_UNIT to Triple(
            BusinessUnitImpl::class,
            BusinessUnit::class,
            BusinessUnitBuilder::class
        ),
        ResourceTypeId.CART_DISCOUNT to Triple(
            CartDiscountImpl::class,
            CartDiscount::class,
            CartDiscountBuilder::class
        ),
        ResourceTypeId.CATEGORY to Triple(CategoryImpl::class, Category::class, CategoryBuilder::class),
        ResourceTypeId.CHANNEL to Triple(ChannelImpl::class, Channel::class, ChannelBuilder::class),
        ResourceTypeId.CUSTOMER to Triple(CustomerImpl::class, Customer::class, CustomerBuilder::class),
        ResourceTypeId.CUSTOMER_GROUP to Triple(
            CustomerGroupImpl::class,
            CustomerGroup::class,
            CustomerGroupBuilder::class
        ),
        ResourceTypeId.CUSTOM_LINE_ITEM to Triple(
            CustomLineItemImpl::class,
            CustomLineItem::class,
            CustomLineItemBuilder::class
        ),
        ResourceTypeId.DISCOUNT_CODE to Triple(
            DiscountCodeImpl::class,
            DiscountCode::class,
            DiscountCodeBuilder::class
        ),
        ResourceTypeId.INVENTORY_ENTRY to Triple(
            InventoryEntryImpl::class,
            InventoryEntry::class,
            InventoryEntryBuilder::class
        ),
        ResourceTypeId.LINE_ITEM to Triple(LineItemImpl::class, LineItem::class, LineItemBuilder::class),
        ResourceTypeId.ORDER_EDIT to Triple(OrderEditImpl::class, OrderEdit::class, OrderEditBuilder::class),
        ResourceTypeId.ORDER_DELIVERY to Triple(DeliveryImpl::class, Delivery::class, DeliveryBuilder::class),
        ResourceTypeId.ORDER_PARCEL to Triple(ParcelImpl::class, Parcel::class, ParcelBuilder::class),
        ResourceTypeId.PAYMENT to Triple(PaymentImpl::class, Payment::class, PaymentBuilder::class),
        ResourceTypeId.PRODUCT_PRICE to Triple(PriceImpl::class, Price::class, PriceBuilder::class),
        ResourceTypeId.PRODUCT_SELECTION to Triple(
            ProductSelectionImpl::class,
            ProductSelection::class,
            ProductSelectionBuilder::class
        ),
        ResourceTypeId.REVIEW to Triple(ReviewImpl::class, Review::class, ReviewBuilder::class),
        ResourceTypeId.SHIPPING to Triple(ShippingImpl::class, Shipping::class, ShippingBuilder::class),
        ResourceTypeId.SHIPPING_METHOD to Triple(
            ShippingMethodImpl::class,
            ShippingMethod::class,
            ShippingMethodBuilder::class
        ),
        ResourceTypeId.SHOPPING_LIST to Triple(
            ShoppingListImpl::class,
            ShoppingList::class,
            ShoppingListBuilder::class
        ),
        ResourceTypeId.STANDALONE_PRICE to Triple(
            StandalonePriceImpl::class,
            StandalonePrice::class,
            StandalonePriceBuilder::class
        ),
        ResourceTypeId.STORE to Triple(StoreImpl::class, Store::class, StoreBuilder::class),
        ResourceTypeId.PAYMENT_INTERFACE_INTERACTION to Triple(
            PaymentAddInterfaceInteractionActionImpl::class,
            PaymentAddInterfaceInteractionAction::class,
            PaymentAddInterfaceInteractionActionBuilder::class
        ),
        ResourceTypeId.SHOPPING_LIST_TEXT_LINE_ITEM to Triple(
            TextLineItemImpl::class,
            TextLineItem::class,
            TextLineItemBuilder::class
        ),
        ResourceTypeId.TRANSACTION to Triple(TransactionImpl::class, Transaction::class, TransactionBuilder::class),
        ResourceTypeId.QUOTE to Triple(QuoteImpl::class, Quote::class, QuoteBuilder::class)
    )

fun typedResourceBuilderExtensionFunctions(
    typedResources: TypedResources,
    typedResource: TypedResource,
    config: Configuration
): Pair<FunSpec, FunSpec> {
    val customFieldType = TypedCustomFields(typedResource.type, config).className

    val build = FunSpec
        .builder("build${typedResource.typedResourceClassName.simpleName}")
        .receiver(typedResources.builder)
        .addCode(
            "return %1T(this.build() as %2T, this.custom as %3T)",
            typedResource.typedResourceClassName,
            typedResources.resourceDefaultImplementation,
            customFieldType
        )
        .returns(typedResource.typedResourceClassName)
        .build()

    val buildUnchecked = FunSpec
        .builder("build${typedResource.typedResourceClassName.simpleName}Unchecked")
        .receiver(typedResources.builder)
        .addCode(
            "return %1T(this.buildUnchecked() as %2T, this.custom as %3T)",
            typedResource.typedResourceClassName,
            typedResources.resourceDefaultImplementation,
            customFieldType
        )
        .returns(typedResource.typedResourceClassName)
        .build()

    return build to buildUnchecked
}

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
            typedResources("order", types, OrderImpl::class, Order::class, OrderBuilder::class, config),
            typedResources("cart", types, CartImpl::class, Cart::class, CartBuilder::class, config),
        )
        ResourceTypeId.ORDER_RETURN_ITEM -> listOf(
            typedResources(
                "line-item-return-item",
                types,
                LineItemReturnItemImpl::class,
                LineItemReturnItem::class,
                LineItemReturnItemBuilder::class,
                config
            ),
            typedResources(
                "custom-line-item-return-item",
                types,
                CustomLineItemReturnItemImpl::class,
                CustomLineItemReturnItem::class,
                CustomLineItemReturnItemBuilder::class,
                config
            ),
        )
        else -> listOfNotNull(
            resourceTypeIdClasses[resourceTypeId]?.let { (resourceTypeDefaultImplementation, resourceInterface, builder) ->
                typedResources(
                    resourceTypeId.jsonName,
                    types,
                    resourceTypeDefaultImplementation,
                    resourceInterface,
                    builder,
                    config
                )
            }
        )
    }

private fun typedResources(
    resourceTypeName: String,
    types: List<Type>,
    resourceTypeDefaultImplementation: KClass<*>,
    resourceInterface: KClass<*>,
    builder: KClass<*>,
    config: Configuration
): TypedResources =
    TypedResources(
        resourceInterface,
        resourceTypeDefaultImplementation,
        builder,
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
        .addModifiers(KModifier.DATA)
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
                .builder("delegate", resourceTypeDefaultImplementation)
                .addModifiers(KModifier.PRIVATE)
                .initializer("delegate")
                .build()
        )
        .addProperty(
            PropertySpec
                .builder("custom", customFieldType)
                .mutable()
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
        .addFunction(
            FunSpec
                .builder("setCustom")
                .addParameter("custom", CustomFields::class)
                .addStatement("this.custom = custom as %T", customFieldType)
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .addType(
            TypeSpec
                .companionObjectBuilder()
                .addProperty(
                    PropertySpec
                        .builder("TYPE_KEY", String::class)
                        .addModifiers(KModifier.CONST)
                        .initializer("%S", config.typeToKey(type))
                        .build()
                )
                .build()
        )
        .build()

    return TypedResource(
        type,
        className,
        resourceType
    )
}