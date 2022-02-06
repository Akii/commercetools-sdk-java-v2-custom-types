package de.akii.commercetoolsplatform.types.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlinx.serialization.decodeFromString

internal class SerializationKtTest {

    @Serializable
    data class ReferenceTypeIdTest(val referenceTypeId: ReferenceTypeId)

    @Test
    fun canParseReferenceTypeId() {
        val result = Json.decodeFromString<ReferenceTypeIdTest>(
            """
                {
                  "referenceTypeId": "key-value-document"
                }
                """.trimIndent()
        )

        assert(result.referenceTypeId == ReferenceTypeId.KeyValueDocument)
    }

}