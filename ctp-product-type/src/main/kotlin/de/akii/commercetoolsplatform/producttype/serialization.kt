package de.akii.commercetoolsplatform.producttype

import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val serializers = mapOf(
    "boolean" to BooleanType.serializer(),
    "text" to TextType.serializer(),
    "ltext" to LocalizableTextType.serializer(),
    "enum" to EnumType.serializer(),
    "lenum" to LocalizableEnumType.serializer(),
    "number" to NumberType.serializer(),
    "money" to MoneyType.serializer(),
    "date" to DateType.serializer(),
    "time" to TimeType.serializer(),
    "datetime" to DateTimeType.serializer(),
    "reference" to ReferenceType.serializer(),
    "set" to SetType.serializer(),
    "nested" to NestedType.serializer()
)

object AttributeTypeSerializer : JsonContentPolymorphicSerializer<AttributeType>(AttributeType::class) {
    override fun selectDeserializer(element: JsonElement) =
        serializers[element.jsonObject["name"]?.jsonPrimitive?.content]!!
}