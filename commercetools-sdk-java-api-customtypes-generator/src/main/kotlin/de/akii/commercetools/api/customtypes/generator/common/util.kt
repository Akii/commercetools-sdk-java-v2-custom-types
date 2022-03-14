package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.type.ResourceTypeId
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

val jsonCreator =
    AnnotationSpec
        .builder(JsonCreator::class)
        .build()

fun deserializeAs(asClassName: ClassName): AnnotationSpec =
    AnnotationSpec
        .builder(JsonDeserialize::class)
        .addMember(CodeBlock.of("`as` = %T::class", asClassName))
        .build()

fun deserializeUsing(asClassName: ClassName): AnnotationSpec =
    AnnotationSpec
        .builder(JsonDeserialize::class)
        .addMember(CodeBlock.of("using = %T::class", asClassName))
        .build()

fun jsonProperty(name: String): AnnotationSpec =
    AnnotationSpec
        .builder(JsonProperty::class)
        .addMember("%S", name)
        .build()

fun resourceTypeIdToClassName(resourceTypeId: ResourceTypeId, config: Configuration): ClassName =
    ClassName(
        "${config.packageName}.custom_fields",
        classNamePrefix(resourceTypeId.jsonName) + "CustomFieldsType"
    )

private fun classNamePrefix(name: String): String =
    name
        .split('-')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }