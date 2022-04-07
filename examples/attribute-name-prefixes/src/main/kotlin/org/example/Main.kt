package org.example

import com.commercetools.api.client.ProjectApiRoot
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.models.product.Product
import com.fasterxml.jackson.databind.ObjectMapper
import io.vrap.rmf.base.client.*
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.example.models.TypedProductApiModule
import org.example.models.product.TypeAProduct
import org.example.models.product.TypeBProduct

val objectMapper: ObjectMapper =
    JsonUtils
        .createObjectMapper()
        .registerModule(TypedProductApiModule())

val apiRoot: ProjectApiRoot =
    ApiRootBuilder.of(MockApiHttpClient(objectMapper))
        .withApiBaseUrl("")
        .withSerializer(ResponseSerializer.of(objectMapper))
        .build("test")

val products: List<Product> = apiRoot
    .products()
    .get()
    .executeBlocking()
    .body
    .results

val typeAProduct = products[0] as TypeAProduct
val typeBProduct = products[1] as TypeBProduct

fun main() {
    println(typeAProduct.masterData.current.masterVariant.typedAttributes.propertyName)
    println(typeBProduct.masterData.current.masterVariant.typedAttributes.propertyName)
}