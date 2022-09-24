package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.order.CustomLineItemReturnItem
import com.commercetools.api.models.order.LineItemReturnItem
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import de.akii.commercetools.api.customtypes.generator.model.TypedResources
import io.vrap.rmf.base.client.utils.Generated

val jsonCreator =
    AnnotationSpec
        .builder(JsonCreator::class)
        .build()

fun deserializeAs(asClassName: ClassName): AnnotationSpec =
    AnnotationSpec
        .builder(JsonDeserialize::class)
        .addMember(CodeBlock.of("`as` = %T::class", asClassName))
        .build()

fun jsonProperty(name: String): AnnotationSpec =
    AnnotationSpec
        .builder(JsonProperty::class)
        .addMember("%S", name)
        .build()

fun classNamePrefix(name: String): String =
    name
        .split('-')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }

fun hasReturnItemResources(typedResources: List<TypedResources>) =
    typedResources
        .map { it.resourceInterface }
        .any { it == LineItemReturnItem::class || it == CustomLineItemReturnItem::class }

val resourceTypeNameToSubPackage: (String) -> String = {
    when (it) {
        "cart" -> "order"
        "line-item-return-item" -> "order"
        "custom-line-item-return-item" -> "order"
        else -> it.split('-').joinToString("_")
    }
}

val generated =
    AnnotationSpec
        .builder(Generated::class)
        .addMember("comments = %S", "https://github.com/Akii/commercetools-sdk-java-v2-custom-types")
        .build()