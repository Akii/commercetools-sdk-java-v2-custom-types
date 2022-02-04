package de.akii.commercetoolsplatform.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlinx.serialization.decodeFromString

internal class SerializationKtTest {

    @Serializable
    data class DateTimeTest(
        @Serializable(CTPLocalDateTimeSerializer::class)
        val dateTime: LocalDateTime)

    @Serializable
    data class LocalizedStringTest(val localizedString: LocalizedString)

    @Serializable
    data class ReferenceTypeIdTest(val referenceTypeId: ReferenceTypeId)

    @Test
    fun canParseDateTime() {
        val result = Json.decodeFromString<DateTimeTest>(
            """
                {
                  "dateTime": "2018-10-12T14:00:00.000Z"
                }
                """.trimIndent()
        )

        assert(result.dateTime == LocalDateTime.of(2018, 10, 12, 14, 0, 0))
    }

    @Test
    fun canParseLocalizedString() {
        val result = Json.decodeFromString<LocalizedStringTest>(
            """
                {
                  "localizedString": {
                    "en_US": "A localized label"
                  }
                }
                """.trimIndent()
        )

        assert(result.localizedString[Locale("en_US")] == "A localized label")
    }

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