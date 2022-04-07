package org.example

import com.commercetools.api.client.ProjectApiRoot
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.models.type.CustomFieldsAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import io.vrap.rmf.base.client.*
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.example.models.TypedResourcesApiModule
import org.example.models.customer.CustomerPreferredShoeSizeCustomer

val objectMapper: ObjectMapper =
    JsonUtils
        .createObjectMapper()
        .registerModule(TypedResourcesApiModule())

val apiRoot: ProjectApiRoot =
    ApiRootBuilder.of(MockApiHttpClient(objectMapper))
        .withApiBaseUrl("")
        .withSerializer(ResponseSerializer.of(objectMapper))
        .build("test")

val customer: CustomerPreferredShoeSizeCustomer = apiRoot
    .customers()
    .withKey("some-key")
    .get()
    .executeBlocking(CustomerPreferredShoeSizeCustomer::class.java)
    .body

fun main() {
    // using the field accessor
    val fieldAccessor = CustomFieldsAccessor(customer.custom)

    println(fieldAccessor.asLocalizedString("preferredShoeSize")!!.get("en"))

    println("----------------")

    // using typed fields
    println(customer.custom.typedFields.preferredShoeSize.get("en"))
}