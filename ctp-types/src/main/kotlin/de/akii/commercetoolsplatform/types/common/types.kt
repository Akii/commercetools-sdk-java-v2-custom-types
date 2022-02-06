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
data class Reference(
    val typeId: ReferenceTypeId,
    val id: String,
    val obj: JsonObject? = null
)