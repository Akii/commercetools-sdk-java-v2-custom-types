package de.akii.commercetools.api.customtypes.generator.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName

val makeParser =
    FunSpec
        .builder("makeParser")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addParameter("codec", ObjectCodec::class.asTypeName().copy(nullable = true))
        .addCode("""
            val p = json?.traverse()
            p?.setCodec(codec)
            p?.nextToken()
            return p
        """.trimIndent())
        .returns(JsonParser::class.asTypeName().copy(nullable = true))
        .build()

val jsonDeserializerType =
    JsonDeserializer::class
        .asTypeName()
        .parameterizedBy(
            WildcardTypeName.producerOf(
            Any::class.asTypeName().copy(nullable = true))
        )
        .copy(nullable = true)