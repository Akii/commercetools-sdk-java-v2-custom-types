package de.akii.commercetoolsplatform.producttype

import de.akii.commercetoolsplatform.common.Reference
import de.akii.commercetoolsplatform.common.ReferenceTypeId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.*
import kotlinx.serialization.decodeFromString

internal class SerializationKtTest {

    val json = Json {
        ignoreUnknownKeys = true
    }

    @Serializable
    data class AttributeConstraintTest(val attributeConstraint: AttributeConstraint)

    @Serializable
    data class TextInputHintTest(val textInputHint: TextInputHint)

    @Test
    fun canParseProductType() {
        val result = json.decodeFromString<ProductType>(
            """
            {
                "id": "e8de347b-38fa-401d-a996-aa118658a90f",
                "version": 17,
                "createdAt": "2022-02-03T18:22:08.738Z",
                "lastModifiedAt": "2022-02-03T18:28:54.162Z",
                "lastModifiedBy": {
                    "isPlatformClient": true,
                    "user": {
                        "typeId": "user",
                        "id": "0d6532d6-28f4-48af-89a9-29b67d154d98"
                    }
                },
                "createdBy": {
                    "isPlatformClient": true,
                    "user": {
                        "typeId": "user",
                        "id": "0d6532d6-28f4-48af-89a9-29b67d154d98"
                    }
                },
                "name": "test",
                "description": "Some Test Product Type",
                "classifier": "Complex",
                "attributes": [
                    {
                        "name": "a-boolean",
                        "label": {
                            "de-DE": "A Boolean",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "boolean"
                        },
                        "attributeConstraint": "None",
                        "isSearchable": true,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "a-text",
                        "label": {
                            "de-DE": "A Text",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "text"
                        },
                        "attributeConstraint": "Unique",
                        "isSearchable": true,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "a-loc-text",
                        "label": {
                            "de-DE": "A Localized Text",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "ltext"
                        },
                        "attributeConstraint": "SameForAll",
                        "isSearchable": false,
                        "inputHint": "MultiLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "an-enum",
                        "label": {
                            "de-DE": "An Enum",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "enum",
                            "values": [
                                {
                                    "key": "Value1",
                                    "label": "Label1"
                                },
                                {
                                    "key": "Value2",
                                    "label": "Label2"
                                }
                            ]
                        },
                        "attributeConstraint": "CombinationUnique",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "a-loc-enum",
                        "label": {
                            "de-DE": "A Localized Enum",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "lenum",
                            "values": [
                                {
                                    "key": "Value1",
                                    "label": {
                                        "de-DE": "Label1 DE",
                                        "en-US": "Label1 EN"
                                    }
                                },
                                {
                                    "key": "Value2",
                                    "label": {
                                        "de-DE": "Label2",
                                        "en-US": "Label2 EN"
                                    }
                                }
                            ]
                        },
                        "attributeConstraint": "None",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "a-number",
                        "label": {
                            "de-DE": "A Number",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "number"
                        },
                        "attributeConstraint": "None",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "some-money",
                        "label": {
                            "de-DE": "Tendies!",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "money"
                        },
                        "attributeConstraint": "None",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "a-date",
                        "label": {
                            "de-DE": "A Date",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "date"
                        },
                        "attributeConstraint": "None",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "a-time",
                        "label": {
                            "de-DE": "Time!",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "time"
                        },
                        "attributeConstraint": "None",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "date-time",
                        "label": {
                            "de-DE": "Date and Time!",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "datetime"
                        },
                        "attributeConstraint": "None",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "a-ref",
                        "label": {
                            "de-DE": "reference",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "reference",
                            "referenceTypeId": "category"
                        },
                        "attributeConstraint": "None",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    },
                    {
                        "name": "ref-set",
                        "label": {
                            "de-DE": "Ref set",
                            "en-US": ""
                        },
                        "inputTip": {
                            "de-DE": "",
                            "en-US": ""
                        },
                        "isRequired": false,
                        "type": {
                            "name": "set",
                            "elementType": {
                                "name": "reference",
                                "referenceTypeId": "product"
                            }
                        },
                        "attributeConstraint": "None",
                        "isSearchable": false,
                        "inputHint": "SingleLine",
                        "displayGroup": "Other"
                    }
                ]
            }
            """.trimIndent()
        )

        assert(result.id == "e8de347b-38fa-401d-a996-aa118658a90f")
        assert(result.version == 17)
    }

    @Test
    fun canParseAttributeConstraint() {
        val result = json.decodeFromString<AttributeConstraintTest>(
                """
                {
                  "attributeConstraint": "None"
                }
                """.trimIndent()
            )

        assert(result.attributeConstraint == AttributeConstraint.None)
    }

    @Test
    fun canParseTextInputHint() {
        val result = json.decodeFromString<TextInputHintTest>(
                """
                {
                  "textInputHint": "SingleLine"
                }
                """.trimIndent()
            )

        assert(result.textInputHint == TextInputHint.SingleLine)
    }

    @Test
    fun canParseBooleanType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "boolean"
                }
                """.trimIndent()
            )

        assert(result == BooleanType)
    }

    @Test
    fun canParseTextType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "text"
                }
                """.trimIndent()
            )

        assert(result == TextType)
    }

    @Test
    fun canParseLocalizableTextType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "ltext"
                }
                """.trimIndent()
            )

        assert(result == LocalizableTextType)
    }

    @Test
    fun canParseEnumType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "enum",
                  "values": [
                    {"key": "key", "label": "label"}
                  ]
                }
                """.trimIndent()
            )

        when (result) {
            is EnumType -> assert(result.values == listOf(PlainEnumValue("key", "label")))
            else -> fail("Failed to parse enum type")
        }
    }

    @Test
    fun canParseLocalizableEnumType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "lenum",
                  "values": [
                    {
                      "key": "key",
                      "label": {
                        "en_US": "A localized label"
                      }
                    }
                  ]
                }
                """.trimIndent()
            )

        val localizedEnumValue = LocalizedEnumValue(
            "key",
            mapOf(
                Locale("en_US") to "A localized label"
            )
        )

        when (result) {
            is LocalizableEnumType -> assert(result.values == listOf(localizedEnumValue))
            else -> fail("Failed to parse enum type")
        }
    }

    @Test
    fun canParseNumberType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "number"
                }
                """.trimIndent()
            )

        assert(result == NumberType)
    }

    @Test
    fun canParseMoneyType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "money"
                }
                """.trimIndent()
            )

        assert(result == MoneyType)
    }

    @Test
    fun canParseDateType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "date"
                }
                """.trimIndent()
            )

        assert(result == DateType)
    }

    @Test
    fun canParseTimeType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "time"
                }
                """.trimIndent()
            )

        assert(result == TimeType)
    }

    @Test
    fun canParseDateTimeType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "datetime"
                }
                """.trimIndent()
            )

        assert(result == DateTimeType)
    }

    @Test
    fun canParseReferenceType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "reference",
                  "referenceTypeId": "key-value-document"
                }
                """.trimIndent()
            )

        assert(result == ReferenceType(ReferenceTypeId.KeyValueDocument))
    }

    @Test
    fun canParseSetType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "set",
                  "elementType": {
                    "name": "boolean"
                  }
                }
                """.trimIndent()
            )

        assert(result == SetType(BooleanType))
    }

    @Test
    fun canParseNestedType() {
        val result = json.decodeFromString<AttributeType>(
                """
                {
                  "name": "nested",
                  "typeReference": {
                    "typeId": "product-type",
                    "id": "someId"
                  }
                }
                """.trimIndent()
            )

        assert(result == NestedType(Reference(ReferenceTypeId.ProductType, "someId")))
    }

}