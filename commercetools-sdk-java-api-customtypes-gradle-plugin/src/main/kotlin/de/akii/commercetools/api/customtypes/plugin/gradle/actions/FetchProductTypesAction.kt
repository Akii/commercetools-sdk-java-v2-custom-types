package de.akii.commercetools.api.customtypes.plugin.gradle.actions

import com.commercetools.api.client.QueryUtils
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.defaultconfig.ServiceRegion
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.akii.commercetools.api.customtypes.plugin.gradle.parameters.RetrieveProductTypesParameters
import io.vrap.rmf.base.client.oauth2.ClientCredentials
import org.gradle.workers.WorkAction

abstract class FetchProductTypesAction : WorkAction<RetrieveProductTypesParameters> {

    override fun execute() {
        val ctpApi = ApiRootBuilder.of()
            .defaultClient(
                ClientCredentials.of()
                    .withClientId(parameters.clientId.get())
                    .withClientSecret(parameters.clientSecret.get())
                    .build(),
                ServiceRegion.valueOf(parameters.serviceRegion.get())
            )
            .build(parameters.projectName.get())

        val productTypes: List<ProductType> = QueryUtils
            .queryAll(
                ctpApi.productTypes().get(),
                java.util.function.Function.identity()
            )
            .toCompletableFuture()
            .get()
            .flatten()

        ObjectMapper()
            .registerModule(JavaTimeModule())
            .writeValue(parameters.productTypesFile.get(), productTypes)
    }

}
