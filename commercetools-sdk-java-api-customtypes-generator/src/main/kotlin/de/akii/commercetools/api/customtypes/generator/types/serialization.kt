package de.akii.commercetools.api.customtypes.generator.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object AttributeTypeSerializer : JsonContentPolymorphicSerializer<AttributeType>(AttributeType::class) {
    override fun selectDeserializer(element: JsonElement) =
        when (val typeName = element.jsonObject["name"]?.jsonPrimitive?.content) {
            "boolean" -> BooleanType.serializer()
            "text" -> TextType.serializer()
            "ltext" -> LocalizableTextType.serializer()
            "enum" -> EnumType.serializer()
            "lenum" -> LocalizableEnumType.serializer()
            "number" -> NumberType.serializer()
            "money" -> MoneyType.serializer()
            "date" -> DateType.serializer()
            "time" -> TimeType.serializer()
            "datetime" -> DateTimeType.serializer()
            "reference" -> ReferenceType.serializer()
            "set" -> SetType.serializer()
            "nested" -> NestedType.serializer()
            else -> error("Unknown AttributeType: $typeName")
        }
}

object ReferenceTypeIdSerializer : KSerializer<ReferenceTypeId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ReferenceTypeId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ReferenceTypeId) =
        encoder.encodeString(value.ctName)

    override fun deserialize(decoder: Decoder): ReferenceTypeId {
        val referenceTypeId = decoder.decodeString()

        return ReferenceTypeId
            .values().first { it.ctName == referenceTypeId }
    }
}