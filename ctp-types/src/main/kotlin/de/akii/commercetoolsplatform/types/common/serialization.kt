package de.akii.commercetoolsplatform.types.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object LocaleSerializer : KSerializer<Locale> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Locale", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Locale) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Locale =
        Locale(decoder.decodeString())
}

object ReferenceTypeIdSerializer : KSerializer<ReferenceTypeId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ReferenceTypeId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ReferenceTypeId) =
        encoder.encodeString(value.ctpName)

    override fun deserialize(decoder: Decoder): ReferenceTypeId {
        val referenceTypeId = decoder.decodeString()

        return ReferenceTypeId
            .values().first { it.ctpName == referenceTypeId }
    }
}

object CTPLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeString(
            value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )

    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

private val serializers = mapOf(
    "centPrecision" to CentPrecisionMoney.serializer(),
    "highPrecision" to HighPrecisionMoney.serializer(),
)

object MoneySerializer : JsonContentPolymorphicSerializer<Money>(Money::class) {
    override fun selectDeserializer(element: JsonElement) =
        serializers[element.jsonObject["name"]?.jsonPrimitive?.content]!!
}