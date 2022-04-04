package org.example

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import io.vrap.rmf.base.client.*
import java.util.concurrent.CompletableFuture

class MockApiHttpClient(private val objectMapper: ObjectMapper) : ApiHttpClient {

    private val foodProduct = javaClass.getResource("/foodProduct.json")

    override fun close() {}

    override fun <O : Any?> execute(
        request: ApiHttpRequest?,
        outputType: Class<O>?
    ): CompletableFuture<ApiHttpResponse<O>> =
        CompletableFuture.completedFuture(
            ApiHttpResponse(
                200,
                ApiHttpHeaders(emptyList()),
                objectMapper.readValue(foodProduct, outputType)
            )
        )

    override fun <O : Any?> execute(
        request: ApiHttpRequest?,
        outputType: TypeReference<O>?
    ): CompletableFuture<ApiHttpResponse<O>> =
        CompletableFuture.completedFuture(
            ApiHttpResponse(
                200,
                ApiHttpHeaders(emptyList()),
                objectMapper.readValue(foodProduct, outputType)
            )
        )

    override fun <O : Any?> execute(
        request: ApiHttpRequest?,
        outputType: JavaType?
    ): CompletableFuture<ApiHttpResponse<O>> =
        CompletableFuture.completedFuture(
            ApiHttpResponse(
                200,
                ApiHttpHeaders(emptyList()),
                objectMapper.readValue(foodProduct, outputType)
            )
        )

    override fun <O : Any?> execute(method: ClientRequestCommand<O>?): CompletableFuture<ApiHttpResponse<O>> {
        TODO("Not yet implemented")
    }

    override fun execute(request: ApiHttpRequest?): CompletableFuture<ApiHttpResponse<ByteArray>> =
        CompletableFuture.completedFuture(
            ApiHttpResponse(
                200,
                ApiHttpHeaders(emptyList()),
                foodProduct.readBytes()
            )
        )

    override fun getSerializerService(): ResponseSerializer =
        ResponseSerializer.of(objectMapper)
}