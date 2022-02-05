package de.akii.commercetoolsplatform.types.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.*

@Serializable(ReferenceTypeIdSerializer::class)
enum class ReferenceTypeId(val ctpName: String) {
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
data class CreatedBy(
    val clientId: String? = null,
    val externalUserId: String? = null,
    val customer: Reference? = null,
    val anonymousId: String? = null,
    val isPlatformClient: Boolean = false,
    val user: Reference? = null
)

@Serializable
data class LastModifiedBy(
    val clientId: String? = null,
    val externalUserId: String? = null,
    val customer: Reference? = null,
    val anonymousId: String? = null,
    val isPlatformClient: Boolean = false,
    val user: Reference? = null
)

@Serializable
data class Reference(
    val typeId: ReferenceTypeId,
    val id: String,
    val obj: JsonObject? = null
)

@Serializable
data class EnumValue(
    val key: String,
    val label: String
)

@Serializable
data class LocalizedEnumValue(
    val key: String,
    val label: LocalizedString
)

@Serializable
sealed class Money

@Serializable
data class CentPrecisionMoney(
    val currencyCode: String,
    val centAmount: Int,
    val fractionDigits: Int
) : Money()

@Serializable
data class HighPrecisionMoney(
    val currencyCode: String,
    val centAmount: Int,
    val preciseAmount: Int,
    val fractionDigits: Int
) : Money()

typealias LocalizedString = Map<@Serializable(LocaleSerializer::class) Locale, String>