@file:OptIn(ExperimentalSerializationApi::class)

package de.akii.commercetoolsplatform.producttype

import de.akii.commercetoolsplatform.common.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import java.time.LocalDateTime

@Serializable
data class ProductType(
    val id: String,
    val key: String? = null,
    val version: Int,
    @Serializable(CTPLocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    val createdBy: CreatedBy? = null,
    @Serializable(CTPLocalDateTimeSerializer::class)
    val lastModifiedAt: LocalDateTime,
    val lastModifiedBy: LastModifiedBy? = null,
    val name: String,
    val description: String,
    val attributes: List<AttributeDefinition>
)

@Serializable
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

@Serializable(AttributeTypeSerializer::class)
sealed class AttributeType

@Serializable
object BooleanType : AttributeType()

@Serializable
object TextType : AttributeType()

@Serializable
object LocalizableTextType : AttributeType()

@Serializable
data class EnumType(val values: List<PlainEnumValue>) : AttributeType()

@Serializable
data class LocalizableEnumType(val values: List<LocalizedEnumValue>) : AttributeType()

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
enum class TextInputHint {
    SingleLine,
    MultiLine
}

@Serializable
enum class AttributeConstraint {
    None,
    Unique,
    CombinationUnique,
    SameForAll
}

@Serializable
data class PlainEnumValue(
    val key: String,
    val label: String
)

@Serializable
data class LocalizedEnumValue(
    val key: String,
    val label: LocalizedString
)