package org.example

import com.commercetools.api.client.ProjectApiRoot
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.models.product.AttributeImpl
import com.commercetools.api.models.product.AttributesAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import io.vrap.rmf.base.client.*
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.example.models.TypedProductApiModule
import org.example.models.product.FoodTypeProduct

val objectMapper: ObjectMapper =
    JsonUtils
        .createObjectMapper()
        .registerModule(TypedProductApiModule())

val apiRoot: ProjectApiRoot =
    ApiRootBuilder.of(MockApiHttpClient(objectMapper))
        .withApiBaseUrl("")
        .withSerializer(ResponseSerializer.of(objectMapper))
        .build("test")

val foodProduct: FoodTypeProduct = apiRoot
    .products()
    .withKey("nutrient-information")
    .get()
    .executeBlocking()
    .body as FoodTypeProduct

val productVariant = foodProduct
    .masterData
    .current
    .masterVariant

val typedAttributes = productVariant.typedAttributes

fun main() {
    // using provided AttributesAccessor
    val taste = productVariant.withProductVariant(AttributesAccessor::of).asString("taste")
    val nutrients = productVariant.withProductVariant(AttributesAccessor::of).get("nutrients")
    val nutrientsValues = nutrients!!.value as List<List<AttributeImpl>>

    println("The taste is $taste for these nutrients:")
    nutrientsValues.forEach {
        println("   Code: ${it[1].value} and Quality: ${it[0].value}")
    }

    println("------------------------------------")

    // using typed attributes
    println("The taste is ${typedAttributes.taste} for these nutrients:")
    typedAttributes.nutrients.forEach { nutrient ->
        println("   Code: ${nutrient.nutrientTypeCode} and Quantity: ${nutrient.quantityContained}")
    }
}