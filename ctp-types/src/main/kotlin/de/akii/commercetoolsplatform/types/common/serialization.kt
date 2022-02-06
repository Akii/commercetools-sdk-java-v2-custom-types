package de.akii.commercetoolsplatform.types.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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