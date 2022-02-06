package de.akii.commercetoolsplatform.types.producttype

import de.akii.commercetoolsplatform.types.common.*
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