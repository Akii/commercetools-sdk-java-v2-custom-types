package de.akii.commercetoolsplatform.generator.producttype

import com.beust.klaxon.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun parser(): Klaxon {
    return Klaxon()
        .converter(localDateTimeConverter)
        .converter(localizedStringConverter)
        .converter(referenceTypeIdConverter)
        .converter(attributeTypeConverter)
}

fun parseProductType(json: String): ProductType {
    return parser().parse<ProductType>(json)!!
}

val attributeTypeConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == AttributeType::class.java

    override fun fromJson(jv: JsonValue) = when (jv.objString("name")) {
        "boolean" -> BooleanType
        "text" -> TextType
        "ltext" -> LocalizableTextType
        "enum" -> parser().parseFromJsonObject<EnumType>(jv.obj!!)!!
        "lenum" -> parser().parseFromJsonObject<LocalizableEnumType>(jv.obj!!)!!
        "number" -> NumberType
        "money" -> MoneyType
        "date" -> DateType
        "time" -> TimeType
        "datetime" -> DateTimeType
        "reference" -> parser().parseFromJsonObject<ReferenceType>(jv.obj!!)!!
        "set" -> parser().parseFromJsonObject<SetType>(jv.obj!!)!!
        "nested" -> parser().parseFromJsonObject<NestedType>(jv.obj!!)!!
        else -> throw KlaxonException("Unsupported AttributeType: ${jv.objString("name")}")
    }

    override fun toJson(value: Any) = TODO("Not implemented")
}

val referenceTypeIdConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == ReferenceTypeId::class.java

    override fun fromJson(jv: JsonValue) = when (jv.string) {
        "cart" -> ReferenceTypeId.Cart
        "cart-discount" -> ReferenceTypeId.CartDiscount
        "category" -> ReferenceTypeId.Category
        "channel" -> ReferenceTypeId.Channel
        "customer" -> ReferenceTypeId.Customer
        "customer-group" -> ReferenceTypeId.CustomerGroup
        "discount-code" -> ReferenceTypeId.DiscountCode
        "key-value-document" -> ReferenceTypeId.KeyValueDocument
        "order" -> ReferenceTypeId.Order
        "payment" -> ReferenceTypeId.Payment
        "product-discount" -> ReferenceTypeId.ProductDiscount
        "product-price" -> ReferenceTypeId.ProductPrice
        "product-selection" -> ReferenceTypeId.ProductSelection
        "product" -> ReferenceTypeId.Product
        "product-type" -> ReferenceTypeId.ProductType
        "review" -> ReferenceTypeId.Review
        "state" -> ReferenceTypeId.State
        "order-edit" -> ReferenceTypeId.OrderEdit
        "shipping-list" -> ReferenceTypeId.ShippingList
        "store" -> ReferenceTypeId.Store
        "tax-category" -> ReferenceTypeId.TaxCategory
        "type" -> ReferenceTypeId.Type
        "shipping-method" -> ReferenceTypeId.ShippingMethod
        "zone" -> ReferenceTypeId.Zone
        else -> throw KlaxonException("Unsupported ReferenceTypeId: ${jv.objString("referenceTypeId")}")
    }

    override fun toJson(value: Any) = TODO("Not implemented")

}

val localDateTimeConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == LocalDateTime::class.java

    override fun fromJson(jv: JsonValue) =
        if (jv.string != null) {
            LocalDateTime.parse(jv.string, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } else {
            throw KlaxonException("Couldn't parse date: ${jv.string}")
        }

    override fun toJson(value: Any) = TODO("Not implemented")
}

val localizedStringConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == LocalizedString::class.java

    override fun fromJson(jv: JsonValue) =
        when (val jsonObject = jv.obj) {
            is JsonObject -> {
                val localeMap = mutableMapOf<Locale, String>()
                jsonObject.forEach {
                    val (locale, label) = it
                    localeMap[Locale(locale)] = label as String
                }
                LocalizedString(localeMap)
            }
            else -> throw KlaxonException("Couldn't parse LocalizedString: $jv")
        }

    override fun toJson(value: Any) = TODO("Not implemented")
}