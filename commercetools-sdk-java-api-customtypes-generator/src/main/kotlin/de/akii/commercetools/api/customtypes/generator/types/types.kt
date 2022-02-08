package de.akii.commercetools.api.customtypes.generator.types

import kotlinx.serialization.Serializable

@Serializable
data class ProductType(
    val id: String,
    val key: String? = null,
    val version: Int,
    val name: String,
    val description: String,
    val attributes: List<AttributeDefinition>
)

@Serializable
data class AttributeDefinition(
    val type: AttributeType,
    val name: String,
    val isRequired: Boolean,
    val attributeConstraint: AttributeConstraint
)

@Serializable(AttributeTypeSerializer::class)
sealed class AttributeType

@Serializable
object BooleanType : AttributeType()

@Serializable
object TextType : AttributeType()

@Serializable
object LocalizableTextType : AttributeType()

@Serializable
object EnumType : AttributeType()

@Serializable
object  LocalizableEnumType : AttributeType()

@Serializable
object NumberType : AttributeType()

@Serializable
object MoneyType : AttributeType()

@Serializable
object DateType : AttributeType()

@Serializable
object TimeType : AttributeType()

@Serializable
object DateTimeType : AttributeType()

@Serializable
data class ReferenceType(val referenceTypeId: ReferenceTypeId) : AttributeType()

@Serializable
data class SetType(val elementType: AttributeType) : AttributeType()

@Serializable
data class NestedType(val typeReference: Reference) : AttributeType()

@Serializable
enum class AttributeConstraint {
    None,
    Unique,
    CombinationUnique,
    SameForAll
}

@Serializable(ReferenceTypeIdSerializer::class)
enum class ReferenceTypeId(val ctName: String) {
    Cart("cart"),
    CartDiscount("cart-discount"),
    Category("category"),
    Channel("channel"),
    Customer("customer"),
    CustomerGroup("customer-group"),
    DiscountCode("discount-code"),
    KeyValueDocument("key-value-document"),
    Payment("payment"),
    Product("product"),
    ProductDiscount("product-discount"),
    ProductPrice("product-price"),
    ProductSelection("product-selection"),
    ProductType("product-type"),
    Order("order"),
    OrderEdit("order-edit"),
    ShippingMethod("shipping-method"),
    ShippingList("shipping-list"),
    State("state"),
    Store("store"),
    TaxCategory("tax-category"),
    Type("type"),
    Review("review"),
    Zone("zone"),
    User("user")
}

@Serializable
data class Reference(
    val typeId: ReferenceTypeId,
    val id: String
)