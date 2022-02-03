package de.akii.commercetoolsplatform.generator.producttype

import java.time.LocalDateTime
import java.util.*

data class ProductType(
    val id: String,
    val key: String? = null,
    val version: Int,
    val createdAt: LocalDateTime,
    val createdBy: CreatedBy?,
    val lastModifiedAt: LocalDateTime,
    val lastModifiedBy: LastModifiedBy?,
    val name: String,
    val description: String,
    val attributes: List<AttributeDefinition>
)

data class AttributeDefinition(
    val type: AttributeType,
    val name: String,
    val label: LocalizedString,
    val isRequired: Boolean,
    val attributeConstraint: AttributeConstraint,
    val inputTip: LocalizedString,
    val inputHint: TextInputHint,
    val isSearchable: Boolean
)

sealed class AttributeType
object BooleanType : AttributeType()
object TextType : AttributeType()
object LocalizableTextType : AttributeType()
data class EnumType(val values: List<PlainEnumValue>) : AttributeType()
data class LocalizableEnumType(val values: List<LocalizedEnumValue>) : AttributeType()
object NumberType : AttributeType()
object MoneyType : AttributeType()
object DateType : AttributeType()
object TimeType : AttributeType()
object DateTimeType : AttributeType()
data class ReferenceType(val referenceTypeId: ReferenceTypeId) : AttributeType()
data class SetType(val elementType: AttributeType) : AttributeType()
data class NestedType(val typeReference: Reference) : AttributeType()

enum class TextInputHint {
    SingleLine,
    MultiLine
}

enum class AttributeConstraint {
    None,
    Unique,
    CombinationUnique,
    SameForAll
}

enum class ReferenceTypeId {
    Cart,
    CartDiscount,
    Category,
    Channel,
    Customer,
    CustomerGroup,
    DiscountCode,
    KeyValueDocument,
    Payment,
    Product,
    ProductDiscount,
    ProductPrice,
    ProductSelection,
    ProductType,
    Order,
    OrderEdit,
    ShippingMethod,
    ShippingList,
    State,
    Store,
    TaxCategory,
    Type,
    Review,
    Zone
}

data class PlainEnumValue(
    val key: String,
    val label: String
)

data class LocalizedEnumValue(
    val key: String,
    val label: LocalizedString
)

data class CreatedBy(
    val clientId: String? = null,
    val externalUserId: String? = null,
    val customer: Reference? = null,
    val anonymousId: String? = null
)

data class LastModifiedBy(
    val clientId: String? = null,
    val externalUserId: String? = null,
    val customer: Reference? = null,
    val anonymousId: String? = null
)

data class Reference(
    val typeId: ReferenceTypeId,
    val id: String
)

data class LocalizedString(private val labels: Map<Locale, String>) : Map<Locale, String> by labels