package org.example

import com.commercetools.api.client.ProjectApiRoot
import com.commercetools.api.defaultconfig.ApiRootBuilder
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

val typedAttributes = foodProduct
    .masterData
    .current
    .masterVariant
    .typedAttributes

fun main() {
    println("The taste is ${typedAttributes.taste} for these nutrients:")
    typedAttributes.nutrients.forEach { nutrient ->
        println("   Code: ${nutrient.nutrientTypeCode} and Quantity: ${nutrient.quantityContained}")
    }
}